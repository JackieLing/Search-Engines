package cn.linghu.dao;

import cn.linghu.pojo.Sku;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class SkuDaoImpl implements SkuDao {

    public List<Sku> querySkuList() {
        // 数据库链接
        Connection connection = null;
        // 预编译statement
        PreparedStatement preparedStatement = null;
        // 结果集
        ResultSet resultSet = null;
        // 商品列表
        List<Sku> list = new ArrayList<Sku>();

        try {
            // 加载数据库驱动
            Class.forName("com.mysql.jdbc.Driver");
            // 连接数据库
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/lucene", "root", "123456");

            // SQL语句
            String sql = "SELECT * FROM tb_sku";
            // 创建preparedStatement
            preparedStatement = connection.prepareStatement(sql);
            // 获取结果集
            resultSet = preparedStatement.executeQuery();
            // 结果集解析
            while (resultSet.next()) {
                Sku sku = new Sku();
                sku.setId(resultSet.getString("id"));
                sku.setName(resultSet.getString("name"));
                sku.setSpec(resultSet.getString("spec"));
                sku.setBrandName(resultSet.getString("brand_name"));
                sku.setCategoryName(resultSet.getString("category_name"));
                sku.setImage(resultSet.getString("image"));
                sku.setNum(resultSet.getInt("num"));
                sku.setPrice(resultSet.getInt("price"));
                sku.setSaleNum(resultSet.getInt("sale_num"));
                list.add(sku);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}
