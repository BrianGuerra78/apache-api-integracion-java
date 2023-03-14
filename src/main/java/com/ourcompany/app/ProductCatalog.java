package com.ourcompany.app;

import com.ourcompany.app.data.Product;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.AdobePDFSchema;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.schema.XMPBasicSchema;
import org.apache.xmpbox.xml.XmpSerializer;

import javax.imageio.ImageIO;
import javax.xml.transform.TransformerException;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.util.Calendar;
import java.util.List;

public class ProductCatalog {
    public void generateCatalog(List<Product> products){//crear PDF con imagenes, titulos y texto
        try(PDDocument catalog = new PDDocument()){
            PDPage firstPage = new PDPage(PDRectangle.A4);
            catalog.addPage(firstPage);
            try(PDPageContentStream content = new PDPageContentStream(catalog, firstPage,PDPageContentStream.AppendMode.APPEND, false)){
                content.beginText();
                content.setFont(PDType1Font.HELVETICA_BOLD,16);
                content.newLineAtOffset(30,750);
                content.showText("FURNITURE - CHAIRS");
                content.endText();

                PDImageXObject prodImage;
                float imageY = 525;
                float nameY = 725;

                content.setLeading(14.5f);

                for(Product p : products) {
                    prodImage = PDImageXObject.createFromFile(p.getImageFile(), catalog);
                    content.drawImage(prodImage, 50, imageY, prodImage.getWidth(), prodImage.getHeight());

                    content.beginText();
                    content.setFont(PDType1Font.HELVETICA_BOLD_OBLIQUE, 14);
                    content.setNonStrokingColor(Color.BLUE);
                    content.newLineAtOffset(300, nameY);
                    content.showText(p.getName());
                    content.newLine();
                    content.setNonStrokingColor(Color.BLACK);
                    for (String feature : p.getFeatures()) {
                        content.showText(feature);
                        content.newLine();
                    }
                    content.endText();
                    content.addRect(300, imageY + 125, 50, 25);
                    //content.setNonStrokingColor(Color.BLACK);     //rellenar el campo
                    //content.fill();
                    content.setStrokingColor(Color.RED);            //contornar el campo
                    content.stroke();

                    content.beginText();
                    content.setFont(PDType1Font.COURIER, 14);
                    //content.setNonStrokingColor(Color.WHITE);
                    content.setNonStrokingColor(Color.BLACK);
                    content.newLineAtOffset(300 + 10, imageY + 133);
                    content.showText("$" + Math.round(p.getPrice()));
                    content.endText();

                    imageY = imageY - prodImage.getHeight();
                    nameY = nameY - prodImage.getHeight();
                }
            }
            catalog.save("src/main/resources/product_catalog.pdf");
            System.out.println("Product Catalog created successfully!");
        } catch(IOException e) {

        }
    }
    public void loadAndModifyProductCatalog(){//agregar paginas en blanco al PDF
        String path = "src/main/resources/product_catalog.pdf";
        File file = new File(path);
        try(PDDocument catalog= PDDocument.load(file)){
            catalog.addPage(new PDPage(PDRectangle.A4));
            catalog.save(path);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public void extractContent(){//extraer imagenes de un PDF
        String path = "src/main/resources/product_catalog.pdf";
        File file= new File(path);
        try(PDDocument catalog = PDDocument.load(file)){
            PDFTextStripper pdfStripper = new PDFTextStripper();
            pdfStripper.getText(catalog);
            String textContent = pdfStripper.getText(catalog);
            System.out.println(textContent);

            PDPage firsPage = catalog.getPage(0);
            PDResources resources= firsPage.getResources();

            int i=1;
            for(COSName name: resources.getXObjectNames()){
                PDXObject xObject = resources.getXObject(name);
                if(xObject instanceof PDImageXObject){
                    PDImageXObject image = (PDImageXObject)xObject;
                    String fileName = "src/main/resources/images/" + "extracted_image_"+ i+ ".jpg";
                    ImageIO.write(image.getImage(), "jpg", new File(fileName));
                    i++;
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public void removePage(){//eliminar paginas de un PDF
        String path = "src/main/resources/product_catalog.pdf";
        File file= new File(path);
        try(PDDocument catalog= PDDocument.load(file)){
          catalog.removePage(1);
          catalog.save(path);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public void setXMPMetadata(){//metadatos de un PDF
        String path= "src/main/resources/product_catalog.pdf";
        File file = new File(path);
        try (PDDocument catalog = PDDocument.load(file)){
            PDDocumentCatalog docCat = catalog.getDocumentCatalog();
            XMPMetadata metadata= XMPMetadata.createXMPMetadata();

            XMPBasicSchema basicSchema = metadata.createAndAddXMPBasicSchema();
            Calendar date = Calendar.getInstance();
            basicSchema.setCreateDate(date);
            basicSchema.setModifyDate(date);

            AdobePDFSchema pdfSchema = metadata.createAndAddAdobePDFSchema();
            pdfSchema.setKeywords("Furniture, Applicances");

            DublinCoreSchema dcSchema = metadata.createAndAddDublinCoreSchema();
            dcSchema.setTitle("Product Catalog - Furniture");
            dcSchema.addCreator("Application");
            dcSchema.setDescription("Product Catalog - Furniture");

            XmpSerializer serializer = new XmpSerializer();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            serializer.serialize(metadata,baos, false);

            PDMetadata pdMetadata = new PDMetadata(catalog);
            pdMetadata.importXMPMetadata(baos.toByteArray());
            docCat.setMetadata(pdMetadata);
            catalog.save(path);

        }catch (IOException | TransformerException e){
            e.printStackTrace();
        }
    }

    public void setImageMetaData(){//Metadatos de las imagenes de un PDF
        String path = "src/main/resources/product_catalog.pdf";
        File file = new File(path);
        try(PDDocument catalog = PDDocument.load(file)){
            File metadata = new File("src/main/resources/metadata.xml");
            try(InputStream is = Files.newInputStream(metadata.toPath())){
                PDMetadata meta = new PDMetadata(catalog,is);

                PDPage firstPAge = catalog.getPage(0);
                PDResources resources = firstPAge.getResources();
                for (COSName name: resources.getXObjectNames()){
                    PDXObject xObject = resources.getXObject(name);
                    if(xObject instanceof PDImageXObject){
                        PDImageXObject image = (PDImageXObject) xObject;
                        image.setMetadata(meta);
                    }
                }
            }
            catalog.save(path);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public void readImageMetaData(){//enviar los metadatos de la imagen
        String path = "src/main/resources/product_catalog.pdf";
        File file= new File(path);
        try(PDDocument catalog = PDDocument.load(file)){
            PDPage firstPage = catalog.getPage(0);
            PDResources resources= firstPage.getResources();

            for(COSName name: resources.getXObjectNames()){
                PDXObject xObject = resources.getXObject(name);
                if(xObject instanceof  PDImageXObject){
                    PDImageXObject image = (PDImageXObject) xObject;
                    PDMetadata metadata = image.getMetadata();
                    try(InputStream is = metadata.createInputStream(); InputStreamReader isr = new InputStreamReader(is); BufferedReader br = new BufferedReader(isr)){
                        br.lines().forEach(System.out::println);
                    }
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void setDocumentInformation(){//metadatos del documento PDF
        String path= "src/main/resources/product_catalog.pdf";
        File file = new File(path);
        try (PDDocument catalog = PDDocument.load(file)){
            PDDocumentInformation pdi= catalog.getDocumentInformation();
            pdi.setAuthor("Brian Guerra");
            pdi.setTitle("Product Catalog - Furniture");
            pdi.setSubject("Product Catalog - Furniture");
            pdi.setKeywords("Furniture, Appliance");

            Calendar date = Calendar.getInstance();
            pdi.setCreationDate(date);
            pdi.setModificationDate(date);

            pdi.setCustomMetadataValue("Website", "http://furniture.com");
            pdi.setCustomMetadataValue("Email", "contact@furniture.com");
            catalog.save(path);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

}
