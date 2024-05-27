package com.niubee.generator;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import org.apache.commons.lang3.StringUtils;

import java.sql.Types;
import java.util.*;

// 代码生成器主类
public class CodeGenerator {

    public static void main(String[] args) {
        //1.子模块部分
        // 模块前缀名
        String modulesPrefix = "gulimall";
        // 模块前后缀分隔符
        String modulesNameSeparator = "-";
        // 模块后缀名
        String[] modulesSuffix = {"coupon", "member", "order", "product", "ware"};

        //2.数据库部分
        // 数据库前缀名
        String dbPrefix = "gulimall";
        // 数据库名前后缀分隔符
        String dbNameSeparator = "_";
        // 数据库后缀名
        String[] dbsSuffix = {"sms", "ums", "oms", "pms", "wms"};
        // 数据库url
        String url = "jdbc:mysql://192.168.56.3:3306/%s?characterEncoding=UTF-8&useUnicode=true&useSSL=false";
        // 数据库用户名和密码
        String userName = "root";
        String pwd = "abc123";

        //3.数据库名与子模块名映射规则
        Map<String, String> moduleLinksDb = new HashMap<>();
        for (int i = 0; i < modulesSuffix.length; i++) {
            moduleLinksDb.put(modulesPrefix + modulesNameSeparator + modulesSuffix[i], dbPrefix + dbNameSeparator + dbsSuffix[i]);
        }

        // 4.自定义属性
        Map<String, Object> infoMap = new HashMap<>();
        // 3.1 java代码的basePackage
        String basePackage = "com.niubee.gulimall";
        infoMap.put("basePackage", basePackage);
        // 3.2工具类
        //工具类模块名
        infoMap.put("utilsModuleName", "gulimall-common");
        // 工具类子包名（主软件包名后面）
        // 如放置统一结果返回对象
        infoMap.put("utilsPackage", "common.utils");
        // 配置类子包名
        // 如knife4j配置类
        infoMap.put("configPackage", "common.config");

        //生成各个模块的代码，要求每个模块对应一个数据库
        for (int i = 0; i < modulesSuffix.length; i++) {
            // 1.子模块名，用于指定生成代码的子模块路径
            String moduleName = modulesPrefix + modulesNameSeparator + modulesSuffix[i];
            infoMap.put("moduleName", moduleName);
            infoMap.put("moduleSuffix", modulesSuffix[i]);

            // 2.子模块对应的数据库名
            //dbName用于application.yml模板文件
            String dbName = moduleLinksDb.get(moduleName);
            infoMap.put("dbName", dbName);

            // 3.子包名（basePackage后的包名），用于放在子包下的类的package语句
            String moduleFollowPackage = modulesSuffix[i];
            infoMap.put("moduleFollowPackage", moduleFollowPackage);

            // 4.application.java类名前缀
            infoMap.put("applicationClassNamePrefix", StringUtils.capitalize(modulesSuffix[i]));

            // 5.数据表前缀，实现根据数据表创建的类名时不包括表前缀
            String tablePrefix = modulesSuffix[i];

            // 6.执行代码生成
            // 设置数据库配置
            FastAutoGenerator.create(String.format(url, dbName), userName, pwd)
                    //6.1全局配置
                    .globalConfig(builder -> {
                        builder
                                // 设置作者
                                .author("hongzhaji")
                                // 开启 swagger 模式
                                .enableSwagger()
                                // 禁止代码生成后弹出文件夹窗口
                                .disableOpenDir()
                                // 指定输出目录
                                .outputDir(System.getProperty("user.dir") //当前项目根模块路径
                                        + String.format("/%s/src/main/java", moduleName) //某个模块java代码路径
                                );
                    })
                    .dataSourceConfig(builder -> builder.typeConvertHandler((globalConfig, typeRegistry, metaInfo) -> {
                        int typeCode = metaInfo.getJdbcType().TYPE_CODE;
                        if (typeCode == Types.SMALLINT) {
                            // 自定义类型转换
                            return DbColumnType.INTEGER;
                        }
                        return typeRegistry.getColumnType(metaInfo);
                    }))
                    .packageConfig(builder -> {
                        builder
                                // 设置父包名
                                .parent(basePackage)
                                // 设置子包模块名
                                .moduleName(moduleFollowPackage)
                                // 设置mapperXml生成路径
                                .pathInfo(Collections.singletonMap(OutputFile.xml, System.getProperty("user.dir") //当前项目根模块路径
                                        + String.format("/%s/src/main/resources/mappers", moduleName)) //某个模块路径
                                );
                    })
                    // 额外属性，传递给自定义模板引擎类、模板文件表达式
                    .injectionConfig(builder -> {
                        builder.customMap(infoMap);
                    })
                    .strategyConfig(builder -> {
                        //一般配置
                        builder
                                // 设置需要生成的表名，all表示当前数据库的全部数据表
                                .addInclude(getTables("all"))
                                // 设置过滤的表前缀，创建的类名不包括表前缀
                                .addTablePrefix(tablePrefix)
                                .enableCapitalMode();
                        //controller策略配置
                        builder.
                                controllerBuilder()
                                // controller都设置为RestController
                                .enableRestStyle()
                                // 覆盖式生成
                                .enableFileOverride();
                        //实体类策略配置
                        builder.
                                entityBuilder().
                                // 覆盖式生成
                                        enableFileOverride()
                                // 开启lombok
                                .enableLombok();
                        //service策略配置
                        builder.
                                serviceBuilder().
                                // 覆盖式生成
                                        enableFileOverride();
                        //mapper策略配置
                        builder.
                                mapperBuilder()
                                // 覆盖式生成
                                .enableFileOverride();
                    })
                    // 手动指定模板
                    .templateConfig(builder -> {
                        //controller模板
                        builder.controller("/templates/controller.java.vm"); // 设置 Controller 模板路径
                        // 其他模板配置...
                    })
                    // 使用引擎模板，默认的是Velocity引擎模板，在此使用自定义的模板引擎，以将指定包名传递给模板文件
                    .templateEngine(new CustomVelocityTemplateEngine())
                    .execute();
        }
    }

    // 处理 all 情况
    protected static List<String> getTables(String tables) {
        return "all".equals(tables) ? Collections.emptyList() : Arrays.asList(tables.split(","));
    }
}
