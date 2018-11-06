package com.asd.crawler;

import com.asd.mapper.CategoryMapper;
import com.asd.mapper.ItemMapper;
import com.asd.pojo.Category;
import com.asd.pojo.Item;
import com.asd.service.ApiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class MyCrawler implements Crawler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyCrawler.class);

    @Autowired
    private ApiService apiService;

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    private static final String HOME_ADDR = "https://www.paramountmerchandise.co.nz/";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public void start() throws Exception {
       getPages();
    }

    private void getPages() throws Exception {
        String url = HOME_ADDR;
        // 获取到页面数据
        String html = this.apiService.doGet(url);
        // 解析html
        Document home = Jsoup.parse(html);
        Elements catagories = home.select(".list-group-item.dropdown-toggle");

        Map<Integer, Category> catagoriesMap = new LinkedHashMap<>();
        Map<String, Item> itemsMap = new HashMap<>();

        for (Element catagory : catagories) {
            String absHref = catagory.attr("abs:href");
            if(absHref.charAt(absHref.length()-1) == '/')
                absHref = absHref.substring(0, absHref.length()-1);
            String catagoryName = absHref.substring(absHref.lastIndexOf('/')+1);
            Category c = new Category();
            c.setCatagoryName(catagoryName);
            c.setUrl(absHref);

            System.out.println("before save : " + c.toString());
            categoryMapper.saveCategory(c);
            System.out.println("after save : " + c.toString());

            //after save the category has the id;
            getItemsOfCatagory(itemsMap, absHref, c);

        }
        System.out.println("----------begin to save items-------------");
        itemMapper.saveItems(itemsMap.values());
    }

    /**
     *  get the items under this catagory
     * @param absHref
     * @return
     */
    private void getItemsOfCatagory(Map<String, Item> itemsMap, String absHref, Category category) {
        //all the items under the catagory
        try {
            String html = this.apiService.doGet(absHref);
            Document document = Jsoup.parse(html);
            Elements elementsLi = document.select(".pagination li");
            if(elementsLi.isEmpty()){
                //only one page
                //save current page's products
                Elements elements = document.select(".thumbnail");
                addItemIntoMap(itemsMap,category,elements);
            }else{
                //more than one page
                for (Element element : elementsLi) {
                    if(!element.text().isEmpty()){
                        //有页码
                        String url = HOME_ADDR  + element.select("a").attr("href");
                        System.out.println(url );
                        String pageHtml = this.apiService.doGet(url);
                        Document pageDocument = Jsoup.parse(pageHtml);
                        Elements pageElements = pageDocument.select(".thumbnail");
                        addItemIntoMap(itemsMap,category,pageElements);


                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addItemIntoMap(Map<String, Item> itemsMap, Category category, Elements pageElements) throws Exception {
        for (Element element : pageElements) {
            Item item = buildItem(element);
            item.getCategories().add(category);
            if(itemsMap.containsKey(item.getSKU())){
                Item i = itemsMap.get(item.getSKU());
                i.getCategories().addAll(item.getCategories());
            }else{
                itemsMap.put(item.getSKU(),item);
            }
        }
    }

    private Item buildItem(Element element) throws Exception {
        Item item = new Item();
        String imageUrl = element.select(".product-image").attr("src");
        LOGGER.debug( "product image url : {}", imageUrl);
        item.setImage(imageUrl);

        Element caption = element.select(".caption").first();
        String name = caption.select("h3 a").text();
        LOGGER.debug("product name : {} ",name);
        item.setName(name);

        String SKU = caption.select("p").first().text();
        SKU = SKU.substring(SKU.lastIndexOf(" ")  + 1);
        LOGGER.debug("SKU : {}", SKU);
        item.setSKU(SKU);

        String packSize = caption.select("p").eq(1).text();
        packSize = packSize.substring(packSize.lastIndexOf(" ") + 1);
       LOGGER.debug("packSize : {}" , packSize);
        item.setPackSize(packSize);

        Element priceEle = caption.select(".price").first();
        System.out.println( priceEle);
        String price = priceEle.select("span").attr("content");
        LOGGER.debug("discountedPrice : {}" , price);
        item.setDiscountedPrice(Double.parseDouble(price));

        Element hidden = element.select("div.savings-container.hidden").first();
        String retailPrice="0";
        try{
            retailPrice  = hidden.select("span").last().text();
            retailPrice = retailPrice.substring(retailPrice.indexOf("$")+1);
        }catch (Exception e){

        }

        LOGGER.debug("retailPrice : {}", retailPrice);
        item.setRetailPrice(Double.parseDouble(retailPrice));

        String savePercent = "0";
        try{
            savePercent = hidden.select("span").first().text();
            savePercent = savePercent.substring(0, savePercent.indexOf("%"));
        }catch (Exception e) {

        }
        LOGGER.debug("savePercent : {}" , Double.parseDouble(savePercent)/100 );
        item.setSavePercent(Double.parseDouble(savePercent)/100);

       // element.select("a").first().attr("href")
        String detail = this.apiService.doGet(element.select("a").first().attr("href"));
        Document detailDoc = Jsoup.parse(detail);
        String barcode = detailDoc.select("#specifications td").eq(3).text();
        LOGGER.debug("barcode : {}" , barcode);
        item.setBarcode(barcode);

        LOGGER.debug("inStock : {}" , "Y");
        item.setInStock("Y");

        String brand = element.select("meta").first().attr("content");
        LOGGER.debug("brand : " , brand);
        item.setBrand(brand);
        System.out.println(item);

        return item;
    }


    /*{
        Item item = new Item();
        item.setImage("/assets/thumb/050536-CAR6U.jpg");
        item.setName("Camelion AA Super Heavy Duty BP4");
        item.setSKU("050536-CAR6U");
        item.setPackSize("12/240");
        item.setDiscountedPrice(1.05);
        item.setRetailPrice(1.74);
        item.setSavePercent(0.4);
        item.setInStock("Y");
        item.setBarcode("873999000555");
        item.setBrand("Camelion");
        Collection<Item> items = new ArrayList<>();
        ((ArrayList<Item>) items).add(item);

        Category category = new Category();
        category.setUrl("https://www.paramountmerchandise.co.nz/NewArrival");
        category.setCatagoryName("NewArrival");
        category.setCatagoryId(1);
        item.getCategories().add(category);

        itemMapper.saveItems(items);

    }*/

}
