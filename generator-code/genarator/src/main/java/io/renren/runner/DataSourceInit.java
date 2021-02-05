package io.renren.runner;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.alibaba.druid.util.DruidDataSourceUtils;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import javax.sql.DataSource;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @Author: lipj
 * @Description:
 * @Date: 2021/2/5 1:26
 */
@Component
@Slf4j
public class DataSourceInit implements CommandLineRunner {

    @Autowired
    DynamicRoutingDataSource dynamicRoutingDataSource;

    @Override
    public void run(String... args) throws Exception {
       String str = FileUtils.readFileToString(
                new File(new UrlResource(ResourceUtils.getURL("classpath:dataSource.json")).
                        getURI().getPath()), "UTF-8");
        List<Map> maps  = (List) JSONArray.parseArray(str);
        maps.forEach(map -> {
            try {
                dynamicRoutingDataSource.addDataSource(map.get("key").toString(),create(map.get("driverClassName").toString(),map.get("url").toString()
                        ,map.get("username").toString(),map.get("password").toString()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    private DataSource create(String driverClassName,String url,String username,String password) throws Exception {
        Properties props = new Properties();
        props.put("driverClassName", driverClassName);
        props.put("url", url);
        props.put("username", username);
        props.put("password", password);
        props.put("maxWait", "20000");
        return DruidDataSourceFactory.createDataSource(props);
    }
}
