package com.ourcompany.app;

import com.ourcompany.app.data.Product;

import java.util.ArrayList;
import java.util.List;

public class App 
{
    public static void main( String[] args ) {
        List<Product> productData = init();
        ProductCatalog productCatalog = new ProductCatalog();
        productCatalog.generateCatalog(productData);
        productCatalog.loadAndModifyProductCatalog();
        productCatalog.extractContent();
        productCatalog.removePage();
        //productCatalog.setXMPMetadata();
        productCatalog.setImageMetaData();
        productCatalog.readImageMetaData();
        productCatalog.setDocumentInformation();
    }
    private static List<Product> init() {//lista con la informacion en texto del PDF
        List<Product> list = new ArrayList<>();
        Product prd1 = new Product();
        prd1.setName("Craftatoz Wooden Chair");
        prd1.setFeatures(List.of("100 % Sheesham wood", "Finest polishing", "Creative curves and contours"));
        prd1.setPrice(399);
        prd1.setImageFile("src/main/resources/images/Craftatoz_Wooden_Chair.jpg");
        list.add(prd1);

        Product prd2 = new Product();
        prd2.setName("Premium Rome Series Wood Chair");
        prd2.setFeatures(List.of("Teak Wood", "Polished", "Protective Foot Glide Insert"));
        prd2.setPrice(299);
        prd2.setImageFile("src/main/resources/images/Premium_Rome_Series_Wood_Chair.jpg");
        list.add(prd2);

        Product prd3 = new Product();
        prd3.setName("Wish Chair - Beech / Natural");
        prd3.setFeatures(List.of("Beech Wood Frame", "Paper Rope Seat", "Steam Bent Back Rail"));
        prd3.setPrice(299);
        prd3.setImageFile("src/main/resources/images/Wish Chair_Beech_Natural.jpg");
        list.add(prd3);

        return list;
    }
}
