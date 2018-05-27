/*
 * Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.ibm.stocator.fs.cos.systemtests;

import java.io.IOException;
import java.util.Hashtable;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.ibm.stocator.fs.common.FileSystemTestUtils.dumpStats;

/**
 * Test the FileSystem#listStatus() operations
 */
public class TestCOSGlobberBracket extends COSFileSystemBaseTest {

  private static Path[] sTestData;
  private static Path[] sEmptyFiles;
  private static byte[] sData = "This is file".getBytes();
  private static Hashtable<String, String> sConf = new Hashtable<String, String>();

  @BeforeClass
  public static void setUpClass() throws Exception {
    sConf.put("fs.stocator.glob.bracket.support", "true");
    createCOSFileSystem(sConf);
    if (sFileSystem != null) {
      createTestData();
    }
  }

  private static void createTestData() throws IOException {

    sTestData = new Path[] {
        new Path(sBaseURI + "/test1/y=2012/a"),
        new Path(sBaseURI + "/test1/y=2014/b"),
        new Path(sBaseURI + "/test1/y=2018/m=12/d=29/data.csv"),
        new Path(sBaseURI + "/test1/y=2018/m=12/d=28/data1.csv"),

        new Path(sBaseURI + "/test1/y=2018/m=10/d=29/data2.json/"
            + "part-00000-9e959568-1cc5-4bc6-966d-9b366be2204c.json"),
        new Path(sBaseURI + "/test1/y=2018/m=10/d=29/data2.json/"
            + "part-00001-9e959568-1cc5-4bc6-966d-9b366be2204c.json"),

        new Path(sBaseURI + "/test1/y=2018/m=10/d=29/data3.json/"
            + "part-00000-9e959568-1cc5-4bc6-966d-9b366be2204c.json"),
        new Path(sBaseURI + "/test1/y=2018/m=10/d=29/data3.json/"
            + "part-00001-9e959568-1cc5-4bc6-966d-9b366be2204c.json"),

        new Path(sBaseURI + "/test1/y=2018/m=10/d=28/data4.json/"
            + "part-00000-86a4f6f6-d172-4cfa-8714-9259c743e5a9-"
            + "attempt_20180503181319_0000_m_000000_0.json"),

        new Path(sBaseURI + "/test1/y=2018/m=10/d=28/data4.json/"
            + "part-00001-86a4f6f6-d172-4cfa-8714-9259c743e5a9-"
            + "attempt_20180503181319_0000_m_000001_0.json")};

    sEmptyFiles = new Path[] {
        new Path(sBaseURI + "/test1/y=2018/m=10/d=29/data2.json"),
        new Path(sBaseURI + "/test1/y=2018/m=10/d=29/data2.json/_SUCCESS"),
        new Path(sBaseURI + "/test1/y=2018/m=10/d=29/data3.json"),
        new Path(sBaseURI + "/test1/y=2018/m=10/d=28/data4.json"),
        new Path(sBaseURI + "/test1/y=2018/m=10/d=28/data4.json/_SUCCESS")};

    for (Path path : sTestData) {
      createFile(path, sData);
    }
    for (Path path : sEmptyFiles) {
      createEmptyFile(path);
    }
  }

  @Test
  public void testListGlobber() throws Exception {
    FileStatus[] paths;
    paths = sFileSystem.globStatus(new Path(getBaseURI(), "test1/*"));
    assertEquals(dumpStats("/test1/*", paths), sTestData.length, paths.length);
    paths = sFileSystem.globStatus(new Path(getBaseURI(), "test1/y=2018/*"));
    assertEquals(dumpStats("/test1/*", paths), 8, paths.length);
    paths = sFileSystem.globStatus(new Path(getBaseURI(), "test1/y=2019/*"));
    assertEquals(dumpStats("/test1/*", paths), 0, paths.length);
  }

  @Test
  public void testAdvancedGlobber() throws Exception {
    FileStatus[] paths;
    paths = sFileSystem.globStatus(new Path(getBaseURI(), "test1/y=2018/m=10/{d=29,d=28}*"));
    assertEquals(dumpStats("test1/y=2018/m=10/{d=29,d=28}*", paths), 6, paths.length);
  }

  @Test(expected = IOException.class)
  public void testBracketSupport() throws Exception {
    Path path = new Path(sBaseURI + "/testBr/{y=2018}/m=10/d=29/data2.json");
    createFile(path, sData);
  }

  @Test
  public void testBracketSupport2() throws Exception {
    FileStatus[] paths;
    paths = sFileSystem.globStatus(new Path(getBaseURI(), "test1/y=2018/m=10/d={29,28}*"));
    assertEquals(dumpStats("test1/y=2018/m=10/d={29,28}*", paths), 6, paths.length);
  }

}
