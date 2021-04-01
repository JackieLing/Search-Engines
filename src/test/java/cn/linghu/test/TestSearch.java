package cn.linghu.test;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLOutput;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试搜索过程
 */
public class TestSearch {

    @Test
    public void testIndexSearch() throws Exception {

        //1. 创建分词器(对搜索的关键词进行分词使用)
        //注意: 分词器要和创建索引的时候使用的分词器一模一样
        Analyzer analyzer = new StandardAnalyzer();

        //2. 创建查询对象,
        //第一个参数: 默认查询域, 如果查询的关键字中带搜索的域名, 则从指定域中查询, 如果不带域名则从, 默认搜索域中查询
        //第二个参数: 使用的分词器
        QueryParser queryParser = new QueryParser("name", analyzer);

        //3. 设置搜索关键词
        //华 OR  为   手   机
        Query query = queryParser.parse("华为手机");

        //4. 创建Directory目录对象, 指定索引库的位置
        Directory dir = FSDirectory.open(Paths.get("E:\\dir"));
        //5. 创建输入流对象
        IndexReader indexReader = DirectoryReader.open(dir);
        //6. 创建搜索对象
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        //7. 搜索, 并返回结果
        //第二个参数: 是返回多少条数据用于展示, 分页使用
        TopDocs topDocs = indexSearcher.search(query, 10);

        //获取查询到的结果集的总数, 打印
        System.out.println("=======count=======" + topDocs.totalHits);

        //8. 获取结果集
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;

        //9. 遍历结果集
        if (scoreDocs != null) {
            for (ScoreDoc scoreDoc : scoreDocs) {
                //获取查询到的文档唯一标识, 文档id, 这个id是lucene在创建文档的时候自动分配的
                int  docID = scoreDoc.doc;
                //通过文档id, 读取文档
                Document doc = indexSearcher.doc(docID);
                System.out.println("==================================================");
                //通过域名, 从文档中获取域值
                System.out.println("===id==" + doc.get("id"));
                System.out.println("===name==" + doc.get("name"));
                System.out.println("===price==" + doc.get("price"));
                System.out.println("===image==" + doc.get("image"));
                System.out.println("===brandName==" + doc.get("brandName"));
                System.out.println("===categoryName==" + doc.get("categoryName"));

            }
        }
        //10. 关闭流
    }


    /**
     * 数值范围查询
     * @throws Exception
     */
    @Test
    public void testRangeQuery() throws Exception {
        //1. 创建分词器(对搜索的关键词进行分词使用)
        //注意: 分词器要和创建索引的时候使用的分词器一模一样
        Analyzer analyzer = new StandardAnalyzer();

        //2. 创建查询对象,
        Query query = IntPoint.newRangeQuery("price", 100, 1000);

        //4. 创建Directory目录对象, 指定索引库的位置
        Directory dir = FSDirectory.open(Paths.get("E:\\dir"));
        //5. 创建输入流对象
        IndexReader indexReader = DirectoryReader.open(dir);
        //6. 创建搜索对象
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        //7. 搜索, 并返回结果
        //第二个参数: 是返回多少条数据用于展示, 分页使用
        TopDocs topDocs = indexSearcher.search(query, 10);

        //获取查询到的结果集的总数, 打印
        System.out.println("=======count=======" + topDocs.totalHits);

        //8. 获取结果集
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;

        //9. 遍历结果集
        if (scoreDocs != null) {
            for (ScoreDoc scoreDoc : scoreDocs) {
                //获取查询到的文档唯一标识, 文档id, 这个id是lucene在创建文档的时候自动分配的
                int  docID = scoreDoc.doc;
                //通过文档id, 读取文档
                Document doc = indexSearcher.doc(docID);
                System.out.println("==================================================");
                //通过域名, 从文档中获取域值
                System.out.println("===id==" + doc.get("id"));
                System.out.println("===name==" + doc.get("name"));
                System.out.println("===price==" + doc.get("price"));
                System.out.println("===image==" + doc.get("image"));
                System.out.println("===brandName==" + doc.get("brandName"));
                System.out.println("===categoryName==" + doc.get("categoryName"));

            }
        }
        //10. 关闭流
    }

    /**
     * 组合查询
     * @throws Exception
     */
    @Test
    public void testBooleanQuery() throws Exception {

        //1. 创建分词器(对搜索的关键词进行分词使用)
        //注意: 分词器要和创建索引的时候使用的分词器一模一样
        Analyzer analyzer = new StandardAnalyzer();

        //2. 创建查询对象,
        Query query1 = IntPoint.newRangeQuery("price", 100, 1000);

        QueryParser queryParser = new QueryParser("name", analyzer);
        //3. 设置搜索关键词
        //华 OR  为   手   机
        Query query2 = queryParser.parse("华为手机");

        //创建布尔查询对象(组合查询对象)
        /**
         *  BooleanClause.Occur.MUST 必须相当于and, 也就是并且的关系
         *  BooleanClause.Occur.SHOULD 应该相当于or, 也就是或者的关系
         *  BooleanClause.Occur.MUST_NOT 不必须, 相当于not, 非
         *  注意: 如果查询条件都是MUST_NOT, 或者只有一个查询条件, 然后这一个查询条件是MUST_NOT则
         *  查询不出任何数据.
         */
        BooleanQuery.Builder query = new BooleanQuery.Builder();
        query.add(query1, BooleanClause.Occur.MUST);
        query.add(query2, BooleanClause.Occur.MUST);


        //4. 创建Directory目录对象, 指定索引库的位置
        Directory dir = FSDirectory.open(Paths.get("E:\\dir"));
        //5. 创建输入流对象
        IndexReader indexReader = DirectoryReader.open(dir);
        //6. 创建搜索对象
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        //7. 搜索, 并返回结果
        //第二个参数: 是返回多少条数据用于展示, 分页使用
        TopDocs topDocs = indexSearcher.search(query.build(), 10);

        //获取查询到的结果集的总数, 打印
        System.out.println("=======count=======" + topDocs.totalHits);

        //8. 获取结果集
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;


        //9. 遍历结果集
        if (scoreDocs != null) {
            for (ScoreDoc scoreDoc : scoreDocs) {
                //获取查询到的文档唯一标识, 文档id, 这个id是lucene在创建文档的时候自动分配的
                int  docID = scoreDoc.doc;
                //通过文档id, 读取文档
                Document doc = indexSearcher.doc(docID);
                System.out.println("==================================================");
                //通过域名, 从文档中获取域值
                System.out.println("===id==" + doc.get("id"));
                System.out.println("===name==" + doc.get("name"));
                System.out.println("===price==" + doc.get("price"));
                System.out.println("===image==" + doc.get("image"));
                System.out.println("===brandName==" + doc.get("brandName"));
                System.out.println("===categoryName==" + doc.get("categoryName"));

            }
        }
        //10. 关闭流
    }


    /**
     * 测试相关度排序
     * @throws Exception
     */
    @Test
    public void testIndexSearch2() throws Exception {

        //1. 创建分词器(对搜索的关键词进行分词使用)
        //注意: 分词器要和创建索引的时候使用的分词器一模一样
        Analyzer analyzer = new IKAnalyzer();

        //需求: 不管是名称域还是品牌域或者是分类域有关于手机关键字的查询出来
        //查询的多个域名
        String[] fields = {"name", "categoryName", "brandName"};

        //设置影响排序的权重, 这里设置域的权重
        Map<String, Float> boots = new HashMap<>();
        boots.put("categoryName", 10000000000f);

        //从多个域查询对象
        MultiFieldQueryParser multiFieldQueryParser = new MultiFieldQueryParser(fields, analyzer, boots);
        //设置查询的关键词
        Query query = multiFieldQueryParser.parse("手机");

        //4. 创建Directory目录对象, 指定索引库的位置
        Directory dir = FSDirectory.open(Paths.get("E:\\dir"));
        //5. 创建输入流对象
        IndexReader indexReader = DirectoryReader.open(dir);
        //6. 创建搜索对象
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        //7. 搜索, 并返回结果
        //第二个参数: 是返回多少条数据用于展示, 分页使用
        TopDocs topDocs = indexSearcher.search(query, 10);

        //获取查询到的结果集的总数, 打印
        System.out.println("=======count=======" + topDocs.totalHits);

        //8. 获取结果集
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;

        //9. 遍历结果集
        if (scoreDocs != null) {
            for (ScoreDoc scoreDoc : scoreDocs) {
                //获取查询到的文档唯一标识, 文档id, 这个id是lucene在创建文档的时候自动分配的
                int  docID = scoreDoc.doc;
                //通过文档id, 读取文档
                Document doc = indexSearcher.doc(docID);
                System.out.println("==================================================");
                //通过域名, 从文档中获取域值
                System.out.println("===id==" + doc.get("id"));
                System.out.println("===name==" + doc.get("name"));
                System.out.println("===price==" + doc.get("price"));
                System.out.println("===image==" + doc.get("image"));
                System.out.println("===brandName==" + doc.get("brandName"));
                System.out.println("===categoryName==" + doc.get("categoryName"));

            }
        }
        //10. 关闭流
    }

}
