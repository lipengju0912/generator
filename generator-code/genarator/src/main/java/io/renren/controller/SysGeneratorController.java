/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.controller;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.annotation.DS;
import io.renren.dynamic.DynamicDto;
import io.renren.service.SysGeneratorService;
import io.renren.utils.PageUtils;
import io.renren.utils.Query;
import io.renren.utils.R;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 代码生成器
 * 
 * @author Mark sunlightcs@gmail.com
 */
@Controller
@RequestMapping("/sys/generator")
public class SysGeneratorController {
	@Autowired
	private SysGeneratorService sysGeneratorService;

	@Autowired
	DynamicRoutingDataSource dynamicRoutingDataSource;

	/**
	 * 配置dataSource配置文件的服务器路径
	 */
	@Value("${dataSource.path}")
	String path;
	/**
	 * 列表
	 */
	@ResponseBody
	@RequestMapping("/list")
	@DS("master")
	public R list(@RequestParam Map<String, Object> params, DynamicDto dynamicDto){
		PageUtils pageUtil = sysGeneratorService.queryList(new Query(params));
		
		return R.ok().put("page", pageUtil);
	}
	
	/**
	 * 生成代码
	 */
	@RequestMapping("/code")
	@DS("master")
	public void code(String tables, HttpServletResponse response,DynamicDto dynamicDto) throws IOException{
		byte[] data = sysGeneratorService.generatorCode(tables.split(","));
		
		response.reset();  
        response.setHeader("Content-Disposition", "attachment; filename=\"renren.zip\"");  
        response.addHeader("Content-Length", "" + data.length);  
        response.setContentType("application/octet-stream; charset=UTF-8");  
  
        IOUtils.write(data, response.getOutputStream());  
	}

	/**
	 * 查找数据源
	 */
	@RequestMapping("/findDataSource")
	@ResponseBody
	public R findDataSource() throws Exception {
		String str = FileUtils.readFileToString(
				new File(path), "UTF-8");
		List<Map> maps  = (List) JSONArray.parseArray(str);
		return R.ok().put("data", maps);
	}

	/**
	 * 删除数据源
	 */
	@RequestMapping("/deleteDataSource")
	@ResponseBody
	public R deleteDataSource(@RequestParam String key) throws Exception {
		if (!StringUtils.hasText(key)) {
			return R.error("key值为空，无法删除数据源！");
		}
		dynamicRoutingDataSource.removeDataSource(key);
		File file = new File(path);
		String str = FileUtils.readFileToString(file, "UTF-8");
		List<Map> maps  = (List) JSONArray.parseArray(str);
		maps.removeIf(map -> key.equals(map.get("key")));
		FileUtils.write(file , maps.toString(),"UTF-8",false);
		return R.ok("成功删除数据源key: " + key);
	}
	/**
	 * 插入数据源
	 */
	@RequestMapping("/insertDataSource")
	@ResponseBody
	public R insertDataSource(String key,String driverClassName,
							  String url,String username,String password) throws Exception {
		if (!StringUtils.hasText(key)) {
			return R.error("key值为空，无法新增数据源！");
		}
		if (!StringUtils.hasText(driverClassName)) {
			return R.error("driverClassName值为空，无法新增数据源！");
		}
		if (!StringUtils.hasText(url)) {
			return R.error("url值为空，无法新增数据源！");
		}
		if (!StringUtils.hasText(username)) {
			return R.error("username值为空，无法新增数据源！");
		}
		if (!StringUtils.hasText(password)) {
			return R.error("password值为空，无法新增数据源！");
		}

		Properties props = new Properties();
		props.put("driverClassName", driverClassName.trim());
		props.put("url", url.trim());
		props.put("username", username.trim());
		props.put("password", password.trim());
		props.put("maxWait", "20000");
		DataSource dataSource = DruidDataSourceFactory.createDataSource(props);

		// 测试是否可以连接
		try {
			dataSource.getConnection();
		} catch (SQLException throwables) {
			return R.error("该数据源无法连接数据库，请检查数据源相关配置！错误信息：" + throwables.getMessage());
		}

		// 添加文件
		File file = new File(path);
		String str = FileUtils.readFileToString(file, "UTF-8");
		List<Map> maps  = (List) JSONArray.parseArray(str);
		for (Map map : maps) {
			if(key.equals(map.get("key"))){
				return R.error("key值已存在，无法新增数据源！");
			}
		}
		Map map = new HashMap<String,Object>();
		map.put("key",key);
		map.put("driverClassName",driverClassName);
		map.put("url",url);
		map.put("username",username);
		map.put("password",password);
		map.put("maxWait","20000");
		maps.add(map);
		FileUtils.write(file , maps.toString(),"UTF-8",false);
		// 添加缓存
		dynamicRoutingDataSource.addDataSource(key,dataSource);
		return R.ok("成功新增数据源key: " + key);
	}

}
