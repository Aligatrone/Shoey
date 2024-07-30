package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
public class CCCScraper {
    public static final ShoeRepository shoeRepository = new ShoeRepository();

    public static boolean runScraper(String link) {
        System.out.println("THE SCRAPPER GOT : "+link);
        try {
            shoeRepository.saveShoes(execute(link));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void main(String args[]) {
        shoeRepository.saveShoes(execute("https://ccc.eu/ro/barbati/pantofi"));

        //System.out.println(replaceRomana("varăîșțâ"));
    }


    public static List<Shoe> execute(String pageUrl) {
        String url = pageUrl;

        List<Shoe> shoes = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(url).get();

            Elements productElements = doc.select("div.c-offerBox.is-hovered");

            for (Element productElement : productElements) {
                Shoe shoe = new Shoe();

                if(url.contains("barbati"))
                    shoe.setGender(Genders.male);
                else shoe.setGender(Genders.female);

                Element divLink = productElement.selectFirst("div.c-offerBox_inner");

                Element link = divLink.selectFirst("a");

                String href = link.attr("href");

                String linkShoe = "https://ccc.eu" + href;
                System.out.println("Href: " + linkShoe);

                shoe.setLink(linkShoe);

                Element divPhoto = productElement.selectFirst("div.c-offerBox_photo");
                Element imgElements = divPhoto.selectFirst("img");
                String linkPhoto = "https://ccc.eu" + imgElements.attr("src");
                if(linkPhoto.equals("https://ccc.eu")) {
                    linkPhoto = "https://ccc.eu" + imgElements.attr("data-src");
                }

                String name = imgElements.attr("alt");

                System.out.println("Img: " + linkPhoto);
                System.out.println("Name: " + name);

                shoe.setName(replaceRomana(name));
                shoe.setPhoto(linkPhoto);

                Element divPrice = productElement.selectFirst("div.a-price_new.is-medium");
                String price = divPrice.attr("data-price");
                char virgula = '.';

                StringBuilder stringBuilder = new StringBuilder(price);
                stringBuilder.insert(stringBuilder.length() - 2, virgula);
                String modPrice = stringBuilder.toString();

                System.out.println("Price: " + modPrice);

                shoe.setPrice(Double.parseDouble(modPrice));


                Document docShoe = Jsoup.connect(linkShoe).get();
                Elements shoeElements = docShoe.select("div.c-offerBox.is-wide");

                if (shoeElements.size() >= 2) {

                    Element secondDiv = shoeElements.get(1);

                    Elements photos = secondDiv.select("div.c-offerBox_galleryItem");

                    List<String> shoeImages = new ArrayList<>();

                    for(Element photo: photos) {
                        Elements divPhotoAddr = photo.select("img");



                        for(Element divPhotoAdd: divPhotoAddr) {
                            String srcValue = divPhotoAdd.attr("data-src");

                            String imgLink = "https://ccc.eu" + srcValue;

                            shoeImages.add(imgLink);
                        }


                    }

                    System.out.println("Images: " + shoeImages);

                    shoe.setImage(shoeImages);

                    Element divInfo = secondDiv.selectFirst("div.c-accordion_contentRaw");

                    //System.out.println(divInfo.toString()); //////
                    Elements tablesInfo = divInfo.select("table.c-table.is-specification");

                    for(Element tableInfo: tablesInfo) {
                        Elements rows = tableInfo.select("tr");

                        for(Element row: rows) {
                            Elements cells = row.select("td");

                            for (int i = 0; i < cells.size() - 1; i += 2) {
                                Element cell1 = cells.get(i);
                                Element cell2 = cells.get(i + 1);

                                // Extract and print the content of the cells
                                String cellContent1 = cell1.text();
                                String cellContent2 = cell2.text();

                                if(cellContent1.equals("Culoare")) {
                                    System.out.println(cellContent1 + ": " + cellContent2);

                                    shoe.setColor(replaceRomana(cellContent2));
                                }

                                if(cellContent1.equals("Sezon")) {
                                    System.out.println(cellContent1 + ": " + cellContent2);

                                    shoe.setSeason(replaceRomana(cellContent2));
                                }

                                if(cellContent1.equals("Stil")) {
                                    System.out.println(cellContent1 + ": " + cellContent2);

                                    shoe.setStyle(replaceRomana(cellContent2));
                                }
                            }
                        }
                    }

                    Elements divSizes = secondDiv.select("div.c-offerBox_variantWrapper");

                    List<String> sizes = new ArrayList<>();

                    for(Element divSize: divSizes) {
                        Element spanSize = divSize.selectFirst("span");

                        String size = spanSize.text();

                        sizes.add(size);
                    }

                    List<Integer> sizesInteger = new ArrayList<>();

                    for(String str: sizes) {
                        int number = Integer.parseInt(str);

                        sizesInteger.add(number);
                    }

                    shoe.setSize(sizesInteger);

                    System.out.println("Sizes: " + sizes);
                }

                System.out.println(shoe.toString());

                System.out.println("------------------------------");

                shoes.add(shoe);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return shoes;
    }

    public static String replaceRomana(String string) {
        return string.replace("ă", "a")
                .replace("î", "i")
                .replace("ș", "s")
                .replace("ț", "t")
                .replace("ş", "s")
                .replace("â", "a");
    }
}
