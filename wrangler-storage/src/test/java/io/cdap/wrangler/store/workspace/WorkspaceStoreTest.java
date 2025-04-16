/*
 * Copyright © 2021 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package io.cdap.wrangler.store.workspace;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;

import io.cdap.cdap.api.NamespaceSummary;
import io.cdap.cdap.test.SystemAppTestBase;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.dataset.workspace.WorkspaceNotFoundException;
import io.cdap.wrangler.proto.workspace.v2.SampleSpec;
import io.cdap.wrangler.proto.workspace.v2.Workspace;
import io.cdap.wrangler.proto.workspace.v2.WorkspaceDetail;
import io.cdap.wrangler.proto.workspace.v2.WorkspaceId;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


public class WorkspaceStoreTest extends SystemAppTestBase {
  private static WorkspaceStore store;

  private String sanitizePath(String path) {
    if (System.getProperty("os.name").toLowerCase().contains("windows")) {
      return path.replace(":", "_").replace("\\", "/");
    }
    return path;
  }

  @BeforeClass
  public static void setupTest() throws Exception {
    getStructuredTableAdmin().create(WorkspaceStore.WORKSPACE_TABLE_SPEC);
    store = new WorkspaceStore(getTransactionRunner());
  }

  @After
  public void cleanupTest() throws Exception {
    store.clear();
  }

  @Test
  public void testNotFoundExceptions() throws Exception {
    WorkspaceId workspace = new WorkspaceId(new NamespaceSummary("default", "", 10L), "workspace");
    try {
      store.getWorkspace(workspace);
      Assert.fail();
    } catch (WorkspaceNotFoundException e) {
      // expected
    }

    try {
      store.updateWorkspace(workspace, Workspace.builder("dummy", workspace.getWorkspaceId()).build());
      Assert.fail();
    } catch (WorkspaceNotFoundException e) {
      // expected
    }

    try {
      store.deleteWorkspace(workspace);
      Assert.fail();
    } catch (WorkspaceNotFoundException e) {
      // expected
    }
  }

  @Test
  public void testCRUD() throws Exception {
    NamespaceSummary namespace = new NamespaceSummary("test", "", 10L);
    String workspaceId = "ws1";
    String name = "workspace1";
    List<String> directives = Collections.emptyList();
    long createdTime = System.currentTimeMillis();
    long updatedTime = System.currentTimeMillis();
    SampleSpec sampleSpec = new SampleSpec("conn", "dummy", "/tmp", ImmutableSet.of());
    JsonObject insights = new JsonObject();
    insights.addProperty("key1", "value1");
    insights.addProperty("key2", "value2");

    // Create workspace
    WorkspaceId id = new WorkspaceId(namespace, workspaceId);
    Workspace workspace = Workspace.builder(name, workspaceId)
        .setDirectives(directives)
        .setCreatedTimeMillis(createdTime)
        .setUpdatedTimeMillis(updatedTime)
        .setSampleSpec(sampleSpec)
        .setInsights(insights)
        .build();
    WorkspaceDetail detail = new WorkspaceDetail(workspace, Collections.emptyList());
    store.saveWorkspace(id, detail);

    // Read workspace
    Workspace readWorkspace = store.getWorkspace(id);
    Assert.assertEquals(name, readWorkspace.getWorkspaceName());
    Assert.assertEquals(workspaceId, readWorkspace.getWorkspaceId());
    Assert.assertEquals(directives, readWorkspace.getDirectives());
    Assert.assertEquals(insights, readWorkspace.getInsights());

    // Update workspace
    String newName = "updated workspace";
    JsonObject newInsights = new JsonObject();
    newInsights.addProperty("key3", "value3");
    Workspace updatedWorkspace = Workspace.builder(newName, workspaceId)
        .setDirectives(directives)
        .setCreatedTimeMillis(createdTime)
        .setUpdatedTimeMillis(updatedTime)
        .setSampleSpec(sampleSpec)
        .setInsights(newInsights)
        .build();
    store.updateWorkspace(id, updatedWorkspace);

    // Verify update
    Workspace readUpdatedWorkspace = store.getWorkspace(id);
    Assert.assertEquals(newName, readUpdatedWorkspace.getWorkspaceName());
    Assert.assertEquals(newInsights, readUpdatedWorkspace.getInsights());

    // Delete workspace
    store.deleteWorkspace(id);
    try {
      store.getWorkspace(id);
      Assert.fail("Expected WorkspaceNotFoundException");
    } catch (WorkspaceNotFoundException e) {
      // Expected
    }
  }

  @Test
  public void testNamespaceGenerations() {
    NamespaceSummary nsGen1 = new NamespaceSummary("ns1", "", 1L);
    NamespaceSummary nsGen2 = new NamespaceSummary("ns1", "", 2L);

    WorkspaceId id1 = new WorkspaceId(nsGen1);
    WorkspaceId id2 = new WorkspaceId(nsGen2, id1.getWorkspaceId());

    // test creation in different namespaces
    Workspace meta1 = Workspace.builder("name1", id1.getWorkspaceId())
                        .setCreatedTimeMillis(0L)
                        .setUpdatedTimeMillis(0L)
                        .build();
    store.saveWorkspace(id1, new WorkspaceDetail(meta1, Collections.emptyList()));

    // test that fetching with a different generation doesn't include the workspace
    try {
      store.getWorkspace(id2);
      Assert.fail();
    } catch (WorkspaceNotFoundException e) {
      // expected
    }

    // test that listing with a different generation doesn't include the workspace
    Assert.assertTrue(store.listWorkspaces(nsGen2).isEmpty());
  }
}
