package cn.linghu.test;

import cn.linghu.dao.SkuDao;
import cn.linghu.dao.SkuDaoImpl;
import cn.linghu.pojo.Sku;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.MMapDirectory;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 索引库维护
 */
public class TestIndexManager {


    /**
     * 创建索引库
     */
    @Test
    public void createIndexTest() throws Exception {
        //1. 采集数据
        SkuDao skuDao = new SkuDaoImpl();
        List<Sku> skuList = skuDao.querySkuList();

        //文档集合
        List<Document> docList = new ArrayList<>();

        for (Sku sku : skuList) {
            //2. 创建文档对象
            Document document = new Document();

            //创建域对象并且放入文档对象中
            /**
             * 是否分词: 否, 因为主键分词后无意义
             * 是否索引: 是, 如果根据id主键查询, 就必须索引
             * 是否存储: 是, 因为主键id比较特殊, 可以确定唯一的一条数据, 在业务上一般有重要所用, 所以存储
             *      存储后, 才可以获取到id具体的内容
             */
            document.add(new StringField("id", sku.getId(), Field.Store.YES));

            /**
             * 是否分词: 是, 因为名称字段需要查询, 并且分词后有意义所以需要分词
             * 是否索引: 是, 因为需要根据名称字段查询
             * 是否存储: 是, 因为页面需要展示商品名称, 所以需要存储
             */
            document.add(new TextField("name", sku.getName(), Field.Store.YES));

            /**
             * 是否分词: 是(因为lucene底层算法规定, 如果根据价格范围查询, 必须分词)
             * 是否索引: 是, 需要根据价格进行范围查询, 所以必须索引
             * 是否存储: 是, 因为页面需要展示价格
             */
            document.add(new IntPoint("price", sku.getPrice()));
            document.add(new StoredField("price", sku.getPrice()));

            /**
             * 是否分词: 否, 因为不查询, 所以不索引, 因为不索引所以不分词
             * 是否索引: 否, 因为不需要根据图片地址路径查询
             * 是否存储: 是, 因为页面需要展示商品图片
             */
            document.add(new StoredField("image", sku.getImage()));

            /**
             * 是否分词: 否, 因为分类是专有名词, 是一个整体, 所以不分词
             * 是否索引: 是, 因为需要根据分类查询
             * 是否存储: 是, 因为页面需要展示分类
             */
            document.add(new StringField("categoryName", sku.getCategoryName(), Field.Store.YES));

            /**
             * 是否分词: 否, 因为品牌是专有名词, 是一个整体, 所以不分词
             * 是否索引: 是, 因为需要根据品牌进行查询
             * 是否存储: 是, 因为页面需要展示品牌
             */
            document.add(new StringField("brandName", sku.getBrandName(), Field.Store.YES));

            //将文档对象放入到文档集合中
            docList.add(document);
        }
        //3. 创建分词器, StandardAnalyzer标准分词器, 对英文分词效果好, 对中文是单字分词, 也就是一个字就认为是一个词.
        Analyzer analyzer = new IKAnalyzer();
        //4. 创建Directory目录对象, 目录对象表示索引库的位置
        Directory  dir = FSDirectory.open(Paths.get("E:\\dir"));
        //5. 创建IndexWriterConfig对象, 这个对象中指定切分词使用的分词器
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        //6. 创建IndexWriter输出流对象, 指定输出的位置和使用的config初始化对象
        IndexWriter indexWriter = new IndexWriter(dir, config);
        //7. 写入文档到索引库
        for (Document doc : docList) {
            indexWriter.addDocument(doc);
        }
        //8. 释放资源
        indexWriter.close();
    }

    /**
     * 索引库修改操作
     * @throws Exception
     */
    @Test
    public void updateIndexTest() throws Exception {
        //需要变更成的内容
        Document document = new Document();

        document.add(new StringField("id", "100000003145", Field.Store.YES));
        document.add(new TextField("name", "xxxx", Field.Store.YES));
        document.add(new IntPoint("price", 123));
        document.add(new StoredField("price", 123));
        document.add(new StoredField("image", "xxxx.jpg"));
        document.add(new StringField("categoryName", "手机", Field.Store.YES));
        document.add(new StringField("brandName", "华为", Field.Store.YES));


        //3. 创建分词器, StandardAnalyzer标准分词器, 对英文分词效果好, 对中文是单字分词, 也就是一个字就认为是一个词.
        Analyzer analyzer = new StandardAnalyzer();
        //4. 创建Directory目录对象, 目录对象表示索引库的位置
        Directory  dir = FSDirectory.open(Paths.get("E:\\dir"));
        //5. 创建IndexWriterConfig对象, 这个对象中指定切分词使用的分词器
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        //6. 创建IndexWriter输出流对象, 指定输出的位置和使用的config初始化对象
        IndexWriter indexWriter = new IndexWriter(dir, config);


        //修改, 第一个参数: 修改条件, 第二个参数: 修改成的内容
        indexWriter.updateDocument(new Term("id", "100000003145"), document);

        //8. 释放资源
        indexWriter.close();
    }

    /**
     * 测试根据条件删除
     * @throws Exception
     */
    @Test
    public void deleteIndexTest() throws Exception {
        //3. 创建分词器, StandardAnalyzer标准分词器, 对英文分词效果好, 对中文是单字分词, 也就是一个字就认为是一个词.
        Analyzer analyzer = new StandardAnalyzer();
        //4. 创建Directory目录对象, 目录对象表示索引库的位置
        Directory  dir = FSDirectory.open(Paths.get("E:\\dir"));
        //5. 创建IndexWriterConfig对象, 这个对象中指定切分词使用的分词器
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        //6. 创建IndexWriter输出流对象, 指定输出的位置和使用的config初始化对象
        IndexWriter indexWriter = new IndexWriter(dir, config);


        //测试根据条件删除
        //indexWriter.deleteDocuments(new Term("id", "100000003145"));

        //测试删除所有内容
        indexWriter.deleteAll();

        //8. 释放资源
        indexWriter.close();
    }


    /**
     * 测试创建索引速度优化
     * @throws Exception
     */
    @Test
    public void createIndexTest2() throws Exception {
        //1. 采集数据
        SkuDao skuDao = new SkuDaoImpl();
        List<Sku> skuList = skuDao.querySkuList();

        //文档集合
        List<Document> docList = new ArrayList<>();

        for (Sku sku : skuList) {
            //2. 创建文档对象
            Document document = new Document();
            document.add(new StringField("id", sku.getId(), Field.Store.YES));
            document.add(new TextField("name", sku.getName(), Field.Store.YES));
            document.add(new IntPoint("price", sku.getPrice()));
            document.add(new StoredField("price", sku.getPrice()));
            document.add(new StoredField("image", sku.getImage()));
            document.add(new StringField("categoryName", sku.getCategoryName(), Field.Store.YES));
            document.add(new StringField("brandName", sku.getBrandName(), Field.Store.YES));

            //将文档对象放入到文档集合中
            docList.add(document);
        }

        long start = System.currentTimeMillis();

        //3. 创建分词器, StandardAnalyzer标准分词器, 对英文分词效果好, 对中文是单字分词, 也就是一个字就认为是一个词.
        Analyzer analyzer = new StandardAnalyzer();
        //4. 创建Directory目录对象, 目录对象表示索引库的位置
        Directory  dir = FSDirectory.open(Paths.get("E:\\dir"));
        //5. 创建IndexWriterConfig对象, 这个对象中指定切分词使用的分词器
        /**
         * 没有优化 小100万条数据, 创建索引需要7725ms
         *
         */
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        //设置在内存中多少个文档向磁盘中批量写入一次数据
        //如果设置的数字过大, 会过多消耗内存, 但是会提升写入磁盘的速度
        //config.setMaxBufferedDocs(500000);
        //6. 创建IndexWriter输出流对象, 指定输出的位置和使用的config初始化对象
        IndexWriter indexWriter = new IndexWriter(dir, config);
        //设置多少给文档合并成一个段文件,数值越大索引速度越快, 搜索速度越慢;  值越小索引速度越慢, 搜索速度越快
        //indexWriter.forceMerge(1000000);
        //7. 写入文档到索引库
        for (Document doc : docList) {
            indexWriter.addDocument(doc);
        }
        //8. 释放资源
        indexWriter.close();
        long end = System.currentTimeMillis();
        System.out.println("=====消耗的时间为:==========" + (end - start) + "ms");
    }

}
