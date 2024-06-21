package com.niubee.generator;

import com.baomidou.mybatisplus.generator.config.builder.ConfigBuilder;
import com.baomidou.mybatisplus.generator.config.builder.CustomFile;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.List;
import java.util.Map;

public class CustomVelocityTemplateEngine extends VelocityTemplateEngine {
    // 手动往模板文件写入信息，以在表达式中使用
    @Override
    public Map<String, Object> getObjectMap(ConfigBuilder config, TableInfo tableInfo) {
        Map<String, Object> objectMap = super.getObjectMap(config, tableInfo);
        //提取数据表对应的实体名
        String entityName = StringUtils.uncapitalize(tableInfo.getEntityName());
        objectMap.put("entityNameLower",entityName);
        entityName = StringUtils.capitalize(entityName);
        objectMap.put("entityNameCapital",entityName);
        objectMap.put("entityType",tableInfo.getEntityName());
        return objectMap;
    }

    // 手动指定模板文件并生成对应类
    @Override
    protected void outputCustomFile(List<CustomFile> customFiles, TableInfo tableInfo, Map<String, Object> objectMap) {
        super.outputCustomFile(customFiles, tableInfo, objectMap);
        Map<String, Object> customMap = ((ConfigBuilder) objectMap.get("config")).getInjectionConfig().getCustomMap();
        // 统一数据类
        String responseDataTemplatePath = "/templates/ResponseData.java.vm";
        String utilsPackagePath = customMap.get("basePackage") + File.separator + customMap.get("utilsPackage");
        utilsPackagePath = utilsPackagePath.replace(".", File.separator);
        String utilsOutputPath = System.getProperty("user.dir") + File.separator + customMap.get("utilsModuleName") + File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator + utilsPackagePath + File.separator;
        outputFile(new File(utilsOutputPath + "ResponseData.java"), objectMap, responseDataTemplatePath, true);

        String sysResponseEnumTemplatePath = "/templates/SysResponseEnum.java.vm";
        outputFile(new File(utilsOutputPath + "SysResponseEnum.java"), objectMap, sysResponseEnumTemplatePath, true);

        // knife4j配置类
        String knife4ConfigEnumTemplatePath = "/templates/Knife4jConfig.java.vm";
        String configPackagePath = customMap.get("basePackage") + File.separator + customMap.get("configPackage");
        configPackagePath = configPackagePath.replace(".", File.separator);
        String configOutputPath = System.getProperty("user.dir") + File.separator + customMap.get("utilsModuleName") + File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator + configPackagePath + File.separator;
        outputFile(new File(configOutputPath + "Knife4jConfig.java"), objectMap, knife4ConfigEnumTemplatePath, true);

        // 为每个子模块生成application.yml
        String dbName = customMap.get("dbName").toString();
        objectMap.put("dbName", dbName);
        String applicationYmlOutputPath = System.getProperty("user.dir") + File.separator + customMap.get("moduleName") + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator;
        String applicationYmlTemplatePath = "/templates/application.yml.vm";
        outputFile(new File(applicationYmlOutputPath + "application.yml"), objectMap, applicationYmlTemplatePath, true);

        // 为每个子模块生成application.java
        String basePackagePath = customMap.get("basePackage") + File.separator + customMap.get("moduleFollowPackage");
        basePackagePath = basePackagePath.replace(".", File.separator);
        String applicationOutputPath = System.getProperty("user.dir") + File.separator + customMap.get("moduleName") + File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator + basePackagePath + File.separator;
        String applicationTemplatePath = "/templates/application.java.vm";
        outputFile(new File(applicationOutputPath + StringUtils.capitalize(customMap.get("moduleSuffix").toString()) + "Application.java"), objectMap, applicationTemplatePath, true);
    }

}
