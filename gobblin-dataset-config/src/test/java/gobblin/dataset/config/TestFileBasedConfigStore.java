package gobblin.dataset.config;


import java.io.*;
import java.util.*;
import java.net.URL;

import com.typesafe.config.*;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;

public class TestFileBasedConfigStore {
  private FileBasedConfigStore cs;

  @BeforeClass
  public void startUp() throws Exception{
    URL url = getClass().getResource("/filebasedConfig/");
    cs = new FileBasedConfigStore(new File(url.getFile()), "test");
    cs.loadConfigs();
  }

  @Test public void testValid() throws Exception {

    // testing dataset related functions
    String urn = "config-ds.a1.a2.a3";
    Config c = cs.getConfig(urn);

//        for(Map.Entry<String, ConfigValue> entry: c.entrySet()){
//          System.out.println("app key: " +entry.getKey() + " ,value:" + entry.getValue());
//        }

    Assert.assertTrue(c.getString("testsubs").equals("foobar20"));
    Assert.assertTrue(c.getBoolean("deleteTarget"));
    Assert.assertTrue(c.getInt("retention")==3);

    Assert.assertTrue(c.getString("keyInA1").equals("valueInA1"));
    Assert.assertTrue(c.getString("keyInA2").equals("valueInA2"));
    
    Assert.assertTrue(c.getString("keyInT2").equals("valueInT2"));
    Assert.assertTrue(c.getString("keyInT3").equals("valueInT3"));
    
    Assert.assertTrue(c.getString("keyInL3").equals("valueInL3"));
    Assert.assertTrue(c.getString("keyInTag1").equals("valueInTag1"));

    List<String> tags = cs.getAssociatedTags(urn);
    //    for(String s: tags){
    //      System.out.println("AAA " + s);
    //    }
    Assert.assertTrue(tags.size()==3);
    Assert.assertTrue(tags.get(0).equals("config-tag.tag1.tag2.tag3"));
    Assert.assertTrue(tags.get(1).equals("config-tag.l1.l2.l3"));
    Assert.assertTrue(tags.get(2).equals("config-tag.t1.t2.t3"));
    
    // testing tag related functions

    String inputTag = "config-tag.l1.l2.l3";
    Map<String, Config> urnConfigMap = cs.getTaggedConfig(inputTag);
    //    for(String s:urnConfigMap.keySet()){
    //      System.out.println("AAA urn is " + s);
    //    }
    Assert.assertTrue(urnConfigMap.size()==3);
    Assert.assertTrue(urnConfigMap.containsKey("config-tag.tag1.tag2"));
    Assert.assertTrue(urnConfigMap.containsKey("config-tag.t1.t2.t3"));
    Assert.assertTrue(urnConfigMap.containsKey("config-ds.a1"));
  }

  @Test(expectedExceptions = TagCircularDependencyException.class)
  public void testCircularDependency() {
    URL url = getClass().getResource("/filebasedConfig/");
    FileBasedConfigStore csbad = new FileBasedConfigStore(new File(url.getFile()), "test");
    csbad.loadConfigs("v1.0.1");

    String urn = "config-ds.a1.a2.a3";
    csbad.getConfig(urn);
  }
  
  @Test(expectedExceptions = TagCircularDependencyException.class)
  public void testCircularDependency2() {
    URL url = getClass().getResource("/filebasedConfig/");
    FileBasedConfigStore csbad = new FileBasedConfigStore(new File(url.getFile()), "test");
    csbad.loadConfigs("v1.0.2");

    String urn = "config-tag.t1.t2.t3";
    csbad.getConfig(urn);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInvalidConfigVersion() {
    URL url = getClass().getResource("/filebasedConfig/");
    FileBasedConfigStore csbad = new FileBasedConfigStore(new File(url.getFile()), "test");
    csbad.loadConfigs("v1.0.9");
  }
  
  @Test (expectedExceptions = ConfigVersionMissMatchException.class)
  public void testVersionMissmatch() {
    URL url = getClass().getResource("/filebasedConfig/");
    FileBasedConfigStore cslatest = new FileBasedConfigStore(new File(url.getFile()), "test");
    cslatest.loadConfigs();

    String urn = "config-ds.a1.a2.a3";
    cslatest.getConfig(urn, "v1.0.2");
  }
}