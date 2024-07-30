package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.URLDataSource;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import javax.persistence.Query;
import java.net.InetSocketAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BackendServer {
    private static final ClientRepository clientRepository=new ClientRepository();
    private static final ShoeRepository shoeRepository = new ShoeRepository();
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);

        server.createContext("/", new MyHandler());

        server.start();
        System.out.println("Server is running on http://localhost:8081");
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(BackendServer::sendNewsletter, 0, 1440, TimeUnit.MINUTES);
    }

    private static void sendNewsletter() {
        // Fetch all emails from the database
        List<String> emails = clientRepository.getAllEmailAddresses();

        // Fetch a random shoe from the database
        Shoe randomShoe = shoeRepository.getRandomShoe();

        // Send the random shoe to all emails
        for (String email : emails) {
            sendEmail(email, randomShoe);
        }
    }

    private static void sendEmail(String recipient, Shoe shoe) {


        String subject = "Today's Shoe Recommendation";
        String cid = UUID.randomUUID().toString(); // Generate a unique Content-ID value

        String body = "<div style=\"display: flex;\">"
                + "<div style=\"flex-shrink: 0;\">"
                + "<img src=\"cid:" + cid + "\" height=\"250px\">"
                + "</div>"
                + "<div style=\"margin-left: 10px;\">"
                + "<p>Don't miss out on the following product we are wholeheartedly recommending you:</p>"
                + "<p><strong>Name:</strong> " + shoe.getName() + "</p>"
                + "<p><strong>Price:</strong> " + shoe.getPrice() + "</p>"
                + "<p><strong>Rating:</strong> " + shoe.getRating() + "</p>"
                + "<p><strong>Season:</strong> " + shoe.getSeason() + "</p>"
                + "<p><strong>Link:</strong> <a href=\"" + shoe.getLink() + "\">" + shoe.getLink() + "</a></p>"
                + "</div>"
                + "</div>"
                + "<p>Best regards,<br>Your Shoe Recommender Team</p>";

        String fromEmail = "no-reply@shoesteam.com";
        String host = "smtp.gmail.com";
        int port = 587; // SMTP port (typically 25 or 587)

        String username = "shopesassistant@gmail.com";
        String password = "ztbmpaqfjiyygeld";

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);

        // Create session with authentication
        Session session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            // Create the email with the embedded image
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            message.setSubject(subject);

            // Create the multipart/mixed container to hold the message content and the image attachment
            MimeMultipart multipart = new MimeMultipart("related");

// Create the HTML body part
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(body, "text/html");

// Add the HTML body part to the multipart container
            multipart.addBodyPart(htmlPart);

// Create the image attachment part
            MimeBodyPart imagePart = new MimeBodyPart();
            DataSource imageDataSource = new URLDataSource(new URL(shoe.getPhoto()));
            imagePart.setDataHandler(new DataHandler(imageDataSource));
            imagePart.setHeader("Content-ID", "<" + cid + ">");

// Add the image attachment part to the multipart container
            multipart.addBodyPart(imagePart);

// Set the multipart content as the message content
            message.setContent(multipart);

// Send the email
            Transport.send(message);
            //// Create message
            //Message message = new MimeMessage(session);
            //message.setFrom(new InternetAddress(fromEmail));
            //message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            //message.setSubject(subject);
            //message.setText(body);
//
            //// Send the message
            //Transport.send(message);

            System.out.println("Email sent successfully to: " + recipient);
        } catch (MessagingException e) {
            System.out.println("Failed to send email. Error message: " + e.getMessage());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");


            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            System.out.println("================================================================");
            System.out.println(method);

            if (method.equals("GET")) {
                if (path.equals("/")) {
                    String response = "Hello, World!";
                    sendResponse(exchange, 200, response);
                } else if (path.equals("/shoes")) {
                    List<Shoe> shoes = shoeRepository.getShoes();

                    Collections.shuffle(shoes);

                    List<Shoe> randomShoes = shoes.subList(0, 7);

                    List<SimpleShoe> simpleShoes = new ArrayList<>();

                    for(Shoe shoe: randomShoes) {
                        simpleShoes.add(new SimpleShoe(shoe));
                    }

                    Gson gson = new GsonBuilder().setPrettyPrinting().create();

                    String json = gson.toJson(simpleShoes);

                    System.out.println("Lista papuci: " + simpleShoes.toString());

                    sendJsonResponse(exchange, 200, json);
                }
                else {
                    String response = "Not found";
                    sendResponse(exchange, 404, response);
                }
            } else if(method.equals("POST")){
                if(path.equals("/login")){
                    InputStreamReader inputStreamReader=new InputStreamReader(exchange.getRequestBody(), "UTF-8");
                    BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
                    String requestBody= bufferedReader.readLine();

                    String email=null;
                    String password=null;

                    try{
                        JSONObject json=new JSONObject(requestBody);
                        email = json.getString("email");
                        password=json.getString("password");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    System.out.println(email+" "+password);

                    int authenticated=clientRepository.checkCredentials(email,password);

                    JSONObject jsonResponse=new JSONObject();
                    try {
                        switch (authenticated) {
                            case 0:
                                jsonResponse.put("message", "User doesn't exist");
                                sendJsonResponse(exchange, 404, jsonResponse.toString());
                                break;
                            case 1:
                                String answer = "Successfully Logged | "+clientRepository.findIdByEmail(email);
                                System.out.println(answer);
                                jsonResponse.put("message", answer);
                                sendJsonResponse(exchange, 200, jsonResponse.toString());
                                break;
                            case 2:
                                jsonResponse.put("message", "Incorrect password");
                                sendJsonResponse(exchange, 401, jsonResponse.toString());
                                break;
                            default:
                                jsonResponse.put("message", "There was an error, try again.");
                                sendJsonResponse(exchange, 500, jsonResponse.toString());
                                break;
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                } else if(path.equals("/register")){
                    InputStreamReader inputStreamReader=new InputStreamReader(exchange.getRequestBody(), "UTF-8");
                    BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
                    String requestBody= bufferedReader.readLine();

                    String email=null;
                    String password=null;
                    String firstName=null;
                    String lastName=null;

                    try{
                        JSONObject json=new JSONObject(requestBody);
                        firstName = json.getString("firstName");
                        lastName = json.getString("lastName");
                        email = json.getString("email");
                        password=json.getString("password");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    System.out.println(firstName+" "+lastName+" "+email+" "+password);

                    int authenticated=clientRepository.checkRegister(firstName,lastName,email,password);

                    JSONObject jsonResponse=new JSONObject();
                    try {
                        switch (authenticated) {
                            case -1:
                                jsonResponse.put("message", "Email already in use");
                                sendJsonResponse(exchange, 200, jsonResponse.toString());
                                break;
                            case -2:
                                jsonResponse.put("message", "Incorrect password");
                                sendJsonResponse(exchange, 200, jsonResponse.toString());
                                break;
                            case 1:
                                jsonResponse.put("message", "Successfully Registered");
                                sendJsonResponse(exchange, 200, jsonResponse.toString());
                                break;
                            default:
                                jsonResponse.put("message", "There was an error, try again.");
                                sendJsonResponse(exchange, 500, jsonResponse.toString());
                                break;
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }


                } else if(path.equals("/refresh")){
                    InputStreamReader inputStreamReader=new InputStreamReader(exchange.getRequestBody(), "UTF-8");
                    BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
                    String requestBody= bufferedReader.readLine();

                    Integer value=null;
                    try{
                        JSONObject json=new JSONObject(requestBody);
                        value=json.getInt("value");
                        System.out.println("REFRESH VALUE GOT: "+value);
                        String email=clientRepository.findEmailById(value);

                        JSONObject jsonResponse=new JSONObject();
                        try{
                            if(email != null) {
                                String answer;
                                if(!clientRepository.getProfilePictureByEmail(email).equals("")) {
                                    String includesPicture = " |--|--|--| " + clientRepository.getProfilePictureByEmail(email);
                                    answer = "Successfully Logged | " + clientRepository.getNameByEmail(email)+includesPicture;
                                } else {
                                    answer = "Successfully Logged | " + clientRepository.getNameByEmail(email);
                                }
                                System.out.println(answer);
                                jsonResponse.put("message", answer);
                                sendJsonResponse(exchange, 200, jsonResponse.toString());
                            } else {
                                throw new RuntimeException("Error finding email by ID");
                            }
                        } catch (JSONException | IOException e) {
                            throw new RuntimeException(e);
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                } else if(path.equals("/userInfo")) {
                    InputStreamReader inputStreamReader=new InputStreamReader(exchange.getRequestBody(), "UTF-8");
                    BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
                    String requestBody= bufferedReader.readLine();

                    Integer value=null;
                    try{
                        JSONObject json=new JSONObject(requestBody);
                        value=json.getInt("actualID");
                        String email=clientRepository.findEmailById(value);
                        String firstName=clientRepository.getNameByEmail(email);
                        String lastName= clientRepository.getLastNameByEmail(email);
                        String password=clientRepository.getPasswordByEmail(email);
                        String profilePicture=clientRepository.getProfilePictureByEmail(email);
                        String theAnswer=email+" | "+firstName+" | "+lastName+" | "+password;
                        if(profilePicture != null){
                            theAnswer=theAnswer+" | "+profilePicture;
                        }
                        JSONObject jsonResponse=new JSONObject();
                        try{
                            if(email != null) {

                                System.out.println("THIS IS THE "+theAnswer);
                                jsonResponse.put("message", theAnswer);
                                sendJsonResponse(exchange, 200, jsonResponse.toString());
                            } else {
                                throw new RuntimeException("Error finding email by ID");
                            }
                        } catch (JSONException | IOException e) {
                            throw new RuntimeException(e);
                        }
                    }catch (JSONException e) {
                        throw new RuntimeException(e);
                    }



                } else if(path.equals("/update")){
                    System.out.println("Should enter here");
                    InputStreamReader inputStreamReader=new InputStreamReader(exchange.getRequestBody(), "UTF-8");
                    BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
                    String requestBody= bufferedReader.readLine();

                    String email=null;
                    String password=null;
                    String firstName=null;
                    String lastName=null;
                    String picture=null;
                    Integer actualId=null;

                    try{
                        JSONObject json=new JSONObject(requestBody);
                        actualId = json.getInt("id");
                        firstName = json.getString("firstName");
                        lastName = json.getString("lastName");
                        email = json.getString("email");
                        password=json.getString("password");
                        picture=json.getString("picture");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    System.out.println(actualId+" "+firstName+" "+lastName+" "+email+" "+password+' '+picture);

                    int authenticated=clientRepository.updateUser(actualId,firstName,lastName,email,password,picture);

                    JSONObject jsonResponse=new JSONObject();
                    try {
                        switch (authenticated) {
                            case 1:
                                String message="Successfully Updated | "+clientRepository.getNameByEmail(email);
                                    jsonResponse.put("message", message);
                                sendJsonResponse(exchange, 200, jsonResponse.toString());
                                break;
                            case 2:
                                String pictureMessage="Successfully Updated | "+clientRepository.getNameByEmail(email)+" |--|--|--| "+clientRepository.getProfilePictureByEmail(email);
                                jsonResponse.put("message", pictureMessage);
                                sendJsonResponse(exchange, 200, jsonResponse.toString());
                                break;
                            default:
                                jsonResponse.put("message", "There was an error, try again.");
                                sendJsonResponse(exchange, 500, jsonResponse.toString());
                                break;
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }


                } else if(path.equals("/checkAdmin")){
                    InputStreamReader inputStreamReader=new InputStreamReader(exchange.getRequestBody(), "UTF-8");
                    BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
                    String requestBody= bufferedReader.readLine();

                    Integer value=null;
                    try{
                        JSONObject json=new JSONObject(requestBody);
                        value=json.getInt("value");
                        System.out.println("AdminCheck GOT: "+value);

                        JSONObject jsonResponse=new JSONObject();
                        try{
                                String answer;
                                if(clientRepository.getAdminStatusById(value)){
                                    answer="TRUE";
                                } else {
                                    answer="FALSE";
                                }
                                System.out.println(answer);
                                jsonResponse.put("message", answer);
                                sendJsonResponse(exchange, 200, jsonResponse.toString());

                        } catch (JSONException | IOException e) {
                            throw new RuntimeException(e);
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                } else if(path.equals("/scrapper")){
                    InputStreamReader inputStreamReader=new InputStreamReader(exchange.getRequestBody(), "UTF-8");
                    BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
                    String requestBody= bufferedReader.readLine();

                    String value=null;
                    try{
                        JSONObject json=new JSONObject(requestBody);
                        value=json.getString("linkValue");
                        System.out.println("Scrapper GOT: "+value);

                        JSONObject jsonResponse=new JSONObject();
                        try{
                            String answer="Success";
                            boolean result=CCCScraper.runScraper(value);
                            if(result){
                                answer="Success";
                            } else {
                                answer="Fail";
                            }
                            System.out.println(answer);
                            jsonResponse.put("message", answer);
                            sendJsonResponse(exchange, 200, jsonResponse.toString());

                        } catch (JSONException | IOException e) {
                            throw new RuntimeException(e);
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                } else if(path.equals("/addShoe")){
                    boolean isValid=true;
                    String postAnswer="";
                    InputStreamReader inputStreamReader = new InputStreamReader(exchange.getRequestBody(), "UTF-8");
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String requestBody = bufferedReader.readLine();
                    String name=null;
                    String link=null;
                    double price=-1;
                    String photoLink=null;
                    String season=null;
                    String style=null;
                    String gender=null;
                    String color=null;
                    try {
                        JSONObject json=new JSONObject(requestBody);
                        JSONObject shoeData = json.getJSONObject("shoeData");
                        name = shoeData.getString("name");//
                        link = shoeData.getString("link");//
                        price = shoeData.getDouble("price");//
                        photoLink = shoeData.getString("photoLink");//
                        season = shoeData.getString("season");//
                        style = shoeData.getString("style");//
                        gender = shoeData.getString("gender");//
                        color = shoeData.getString("color");//

                        JSONArray sizesArray = shoeData.getJSONArray("sizes");
                        List<Integer> sizes = new ArrayList<>();
                        for (int i = 0; i < sizesArray.length() && isValid; i++) {
                            try {
                                Integer sizeValue = Integer.parseInt(sizesArray.getString(i));
                                if (sizeValue >= 34 && sizeValue <= 48) {
                                    sizes.add(sizeValue);
                                } else {
                                    isValid = false;
                                }
                            } catch(NumberFormatException e){
                                isValid = false;
                                break;
                            }
                        }
                        System.out.println(name + " " + link + " " + price + " " + photoLink + " " + season + " " + style + " " + gender + " " + color);
                        for(int i=0;i< sizes.size();i++){
                            System.out.println("SIZE : "+ sizes.get(i));
                        }

                        JSONObject jsonResponse=new JSONObject();
                        if(!isValid){
                            postAnswer="One or more sizes are not valid";
                            System.out.println(postAnswer);
                            jsonResponse.put("message", postAnswer);
                            sendJsonResponse(exchange, 200, jsonResponse.toString());
                        } else {

                            Shoe shoe = new Shoe();
                            shoe.setName(name);
                            shoe.setColor(color);
                            shoe.setPhoto(photoLink);
                            shoe.setImage(List.of(photoLink));
                            shoe.setLink(link);
                            if (gender.equals("female")) {
                                shoe.setGender(Genders.female);
                            } else {
                                shoe.setGender(Genders.male);
                            }
                            shoe.setPrice(price);
                            shoe.setSeason(season);
                            shoe.setStyle(style);
                            shoe.setSize(sizes);
                            shoeRepository.saveShoes(List.of(shoe));


                            postAnswer = "Success";

                            System.out.println(postAnswer);
                            jsonResponse.put("message", postAnswer);
                            sendJsonResponse(exchange, 200, jsonResponse.toString());
                        }

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else if(path.equals("/search")) {
                    InputStreamReader inputStreamReader=new InputStreamReader(exchange.getRequestBody(), "UTF-8");
                    BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
                    String requestBody = bufferedReader.readLine();

                    System.out.println("daaaaaa");

                    String value = null;
                    try{
                        JSONObject json=new JSONObject(requestBody);
                        System.out.println(json.toString());

                        value = json.getString("lastString");

                        System.out.println("Search link: " + value);

                        JSONObject jsonResponse=new JSONObject();
                        try{
                            List<Shoe> shoes = shoeRepository.getShoesByParam(value);

                            System.out.println("aiciicicic: " + shoes.toString());

                            Collections.shuffle(shoes);

                            List<SimpleShoe> simpleShoes = new ArrayList<>();

                            for(Shoe shoe: shoes) {
                                simpleShoes.add(new SimpleShoe(shoe));
                            }

                            Gson gson = new GsonBuilder().setPrettyPrinting().create();

                            String result = gson.toJson(simpleShoes);

                            System.out.println("Lista papuci: " + simpleShoes.toString());

                            sendJsonResponse(exchange, 200, result);

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else if(path.equals("/testForm")) {
                    String formData = readFormData(exchange);

                    String boundary = extractBoundary(formData);
                    Map<String, String> formParams = parseFormData(formData, boundary);

                    System.out.println("a mers: " + formParams.get("q1"));

                    Map<String, String> q1Values = new HashMap<>();
                    q1Values.put("a", "male");
                    q1Values.put("b", "female");

                    ShoeFilter filter = new ShoeFilter(q1Values.get(formParams.get("q1")));

                    // Perform the shoe search and populate the jsonResponse object accordingly
                    List<Shoe> resultedShoes = shoeRepository.findShoes(filter);

                    Gson gson = new GsonBuilder().setPrettyPrinting().create();

                    String result = gson.toJson(resultedShoes);

                    sendJsonResponse(exchange, 200, result);

                } else if (path.equals("/searchShoes")) {
                    InputStreamReader inputStreamReader = new InputStreamReader(exchange.getRequestBody(), "UTF-8");
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String requestBody = bufferedReader.readLine();

                    String gender = null;
                    String mainUse = null;
                    String mileage = null;
                    String goals = null;
                    String archShape = null;
                    String support = null;
                    String pain = null;
                    String cushion = null;
                    int size = 0;

                    try {
                        JSONObject json = new JSONObject(requestBody);
                        gender = json.getString("gender");
                        mainUse = json.getString("mainUse");
                        mileage = json.getString("mileage");
                        goals = json.getString("goals");
                        archShape = json.getString("archShape");
                        support = json.getString("support");
                        pain = json.getString("pain");
                        cushion = json.getString("cushion");
                        size = json.getInt("size");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    System.out.println(gender + " " + mainUse + " " + mileage + " " + goals + " " + archShape + " " +
                            support + " " + pain + " " + cushion + " " + size);

                    ShoeFilter filter = new ShoeFilter(gender);
                    // Perform the shoe search and populate the jsonResponse object accordingly
                    List<Shoe> resultedShoes = shoeRepository.findShoes(filter);
                    SimplifyAndSend(exchange, resultedShoes);
                } else if(path.equals("/images")){
                    InputStreamReader inputStreamReader=new InputStreamReader(exchange.getRequestBody(), "UTF-8");
                    BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
                    String requestBody= bufferedReader.readLine();

                    Integer value=null;
                    try{
                        JSONObject json=new JSONObject(requestBody);
                        value=json.getInt("thisID");

                        try{
                            String answer="";
                            List<String> imageUrls = shoeRepository.getAllPhotos(value);
                            if(imageUrls !=null){
                                answer="Success";
                            } else {
                                answer="Failure";
                            }

                            Gson gson = new Gson();
                            String jsonResponse = gson.toJson(imageUrls);

                            System.out.println("returned links : ");
                            sendJsonResponse(exchange, 200, jsonResponse);

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                } else if(path.equals("/shoeInfo")) {
                    InputStreamReader inputStreamReader=new InputStreamReader(exchange.getRequestBody(), "UTF-8");
                    BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
                    String requestBody= bufferedReader.readLine();

                    Integer value=null;
                    Integer userId=null;
                    try{
                        JSONObject json=new JSONObject(requestBody);
                        value=json.getInt("shoeId");

                        String name=null;
                        String gender=null;
                        Double rating=null;
                        Integer ratingNumber=null;
                        Double price=null;
                        String link=null;
                        String color=null;
                        String season=null;
                        String style=null;
                        String sizes=null;
                        JSONObject jsonResponse=new JSONObject();
                        try{
                            Gson gson=new Gson();
                            String finalAnswer="";
                            Shoe shoe=shoeRepository.getShoeById(value);

                            Integer valueOfUserRating=0;
                            Integer isProductLiked=0;
                            if(json.has("actualUserId")){
                                userId=json.getInt("actualUserId");
                                valueOfUserRating=clientRepository.getUserRatingByUserId(userId,shoe);
                                isProductLiked=clientRepository.getUserLikeByUserId(userId,shoe);
                            }

                            if(shoe!=null) {
                                name = shoe.getName();
                                if (shoe.getGender() == Genders.female) {
                                    gender = "female";
                                } else {
                                    gender = "male";
                                }
                                rating = shoe.getRating();
                                ratingNumber = shoe.getNumberOfRatings();
                                price = shoe.getPrice();
                                link = shoe.getLink();
                                season = shoe.getSeason();
                                style = shoe.getStyle();
                                color = shoe.getColor();

                                List<Integer> sizesList=shoe.getSize();
                                Integer[] sizesArray=sizesList.toArray(new Integer[sizesList.size()]);
                                sizes=gson.toJson(sizesArray);

                                finalAnswer = name + " | " + gender + " | " + rating + " | " + ratingNumber + " | " + price + " | " + link
                                        + " | " + season + " | " + style + " | " + color+ " | " + valueOfUserRating+ " | " + isProductLiked;
                            } else {
                                finalAnswer="FailureToTakeData";
                            }
                                System.out.println("trying to access shoe data ");
                            System.out.println(finalAnswer);
                                jsonResponse.put("message", finalAnswer);
                                jsonResponse.put("sizes",sizes);
                                sendJsonResponse(exchange, 200, jsonResponse.toString());
                        } catch (JSONException | IOException e) {
                            throw new RuntimeException(e);
                        }
                    }catch (JSONException e) {
                        throw new RuntimeException(e);
                    }



                } else if(path.equals("/rating")){
                    InputStreamReader inputStreamReader=new InputStreamReader(exchange.getRequestBody(), "UTF-8");
                    BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
                    String requestBody= bufferedReader.readLine();

                    Integer value=null;
                    Integer userId=null;
                    Integer shoeId=null;
                    try{
                        String answer="";
                        JSONObject json=new JSONObject(requestBody);
                        userId=json.getInt("userId");
                        shoeId=json.getInt("shoeId");
                        value=json.getInt("value");

                        System.out.println("Rating endpoint : "+userId+" "+shoeId+" "+value);
                        User user=clientRepository.getUserById(userId);
                        Shoe shoe=shoeRepository.getShoeById(shoeId);

                        if(user != null && shoe != null){
                            boolean isAlreadyRated = user.checkAlreadyRated(shoe);
                            if(isAlreadyRated){
                                Integer ratingForShoe = user.getRatingForShoe(shoe);
                                shoe.addRating(value,false,ratingForShoe);
                                user.changeShoeRating(shoe,value);
                                answer="Rating updated";

                            } else {
                                shoe.addRating(value, true,-1);
                                user.addRatedShoe(shoe,value);
                                answer="Rating added";
                            }
                            clientRepository.reSaveUser(user);
                            shoeRepository.reSaveShoe(shoe);

                        } else {
                            answer="Failure trying to rate the product";
                        }

                        JSONObject jsonResponse=new JSONObject();
                        try{
                            System.out.println(answer);
                            jsonResponse.put("message", answer);
                            sendJsonResponse(exchange, 200, jsonResponse.toString());

                        } catch (JSONException | IOException e) {
                            throw new RuntimeException(e);
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                } else if(path.equals("/delete")){
                    InputStreamReader inputStreamReader=new InputStreamReader(exchange.getRequestBody(), "UTF-8");
                    BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
                    String requestBody= bufferedReader.readLine();

                    Integer value=null;
                    try{
                        JSONObject json=new JSONObject(requestBody);
                        value=json.getInt("shoeId");
                        JSONObject jsonResponse=new JSONObject();
                        try{

                            String answer="";
                            if(shoeRepository.deleteShoeById(value)){
                                answer="Product Deleted Successfully";
                            } else {
                                answer="Failure trying to delete the product";
                            }

                            System.out.println("result : "+answer);
                            jsonResponse.put("message", answer);
                            sendJsonResponse(exchange, 200, jsonResponse.toString());

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                } else if(path.equals("/updateShoe")){
                    System.out.println("Shoe update");
                    InputStreamReader inputStreamReader=new InputStreamReader(exchange.getRequestBody(), "UTF-8");
                    BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
                    String requestBody= bufferedReader.readLine();

                    String name=null;
                    String gender=null;
                    Double price=null;
                    String season=null;
                    String style=null;
                    String sizes=null;
                    Integer actualId=null;

                    try{
                        JSONObject json=new JSONObject(requestBody);
                        actualId = json.getInt("shoeId");
                        name = json.getString("name");
                        gender = json.getString("gender");
                        price = json.getDouble("price");
                        season=json.getString("season");
                        style=json.getString("style");
                        sizes=json.getString("size");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    int authenticated;
                    Shoe shoe=shoeRepository.getShoeById(actualId);

                    boolean sizeEquals=true;
                    List<Integer> shoeSizes=shoe.getSize();
                    String[] sizeArray = sizes.split(",");
                    List<Integer> integerList = new ArrayList<>();
                    for(String size : sizeArray){
                        int number=Integer.parseInt(size.trim());
                        integerList.add(number);
                        if(!(shoeSizes.contains(number))){
                            sizeEquals=false;
                        }
                    }
                    if(sizeArray.length != shoe.getSize().size()){
                        sizeEquals=false;
                    }


                    if(!(shoe.getName().equals(name) && shoe.getPrice().equals(price) && shoe.getSeason().equals(season) && shoe.getStyle().equals(style) && shoe.getGender().toString().equals(gender) && sizeEquals)){
                        shoe.setName(name);
                        shoe.setPrice(price);
                        shoe.setSeason(season);
                        shoe.setStyle(style);
                        if(gender.equals("female")){
                            shoe.setGender(Genders.female);
                        } else {
                            shoe.setGender(Genders.male);
                        }
                        shoe.setSize(integerList);
                        shoeRepository.reSaveShoe(shoe);
                        authenticated=1;
                    } else {
                        authenticated=0;
                    }

                    JSONObject jsonResponse=new JSONObject();
                    try {
                        switch (authenticated) {
                            case 1:
                                String message="Successfully Updated";
                                jsonResponse.put("message", message);
                                sendJsonResponse(exchange, 200, jsonResponse.toString());
                                break;
                            default:
                                jsonResponse.put("message", "There are no new changes");
                                sendJsonResponse(exchange, 200, jsonResponse.toString());
                                break;
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }


                } else if(path.equals("/likeShoe")){
                    InputStreamReader inputStreamReader=new InputStreamReader(exchange.getRequestBody(), "UTF-8");
                    BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
                    String requestBody= bufferedReader.readLine();

                    Integer userId=null;
                    Integer shoeId=null;
                    try{
                        String answer="";
                        JSONObject json=new JSONObject(requestBody);
                        userId=json.getInt("userId");
                        shoeId=json.getInt("shoeId");

                        System.out.println("Liked endpoint : "+userId+" "+shoeId);
                        User user=clientRepository.getUserById(userId);
                        Shoe shoe=shoeRepository.getShoeById(shoeId);

                        if(user != null && shoe != null){
                            if(user.getLikedShoes().contains(shoe)){
                                answer="Product unliked";
                                user.removeLikedShoe(shoe);
                            } else {
                                answer="Product liked";
                                user.addLikedShoe(shoe);
                            }
                            clientRepository.reSaveUser(user);
                        } else {
                            answer="Failure trying to rate the product";
                        }

                        JSONObject jsonResponse=new JSONObject();
                        try{
                            System.out.println(answer);
                            jsonResponse.put("message", answer);
                            sendJsonResponse(exchange, 200, jsonResponse.toString());

                        } catch (JSONException | IOException e) {
                            throw new RuntimeException(e);
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                } else if(path.equals("/getLiked")){
                    InputStreamReader inputStreamReader=new InputStreamReader(exchange.getRequestBody(), "UTF-8");
                    BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
                    String requestBody= bufferedReader.readLine();

                    Integer value=null;
                    try{
                        JSONObject json=new JSONObject(requestBody);
                        value=json.getInt("actualID");
                        JSONObject jsonResponse=new JSONObject();
                        List<String> names=clientRepository.getLikedShoeNames(value);
                        List<String> photos=clientRepository.getLikedShoePhotos(value);
                        List<Integer> shoeId=clientRepository.getLikedShoeIds(value);

                        String answer;
                        if(names!=null && photos!=null){
                            answer="Success";
                            jsonResponse.put("names",new JSONArray(names));
                            jsonResponse.put("photos",new JSONArray(photos));
                            Gson gson = new Gson();
                            String idsJson = gson.toJson(shoeId);
                            jsonResponse.put("ids", new JSONArray(idsJson));
                            System.out.println("the gotten id: "+shoeId.get(0));
                        } else {
                            answer="Failure";
                            jsonResponse.put("message", answer);
                        }
                        System.out.println("result : "+jsonResponse.toString());
                         sendJsonResponse(exchange, 200, jsonResponse.toString());

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                } else if(path.equals("/resetPassword")){
                    InputStreamReader inputStreamReader=new InputStreamReader(exchange.getRequestBody(), "UTF-8");
                    BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
                    String requestBody= bufferedReader.readLine();

                    String email=null;

                    try{
                        JSONObject json=new JSONObject(requestBody);
                        email = json.getString("email");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    System.out.println(email);

                    User authenticated=clientRepository.findByEmail(email);

                    JSONObject jsonResponse=new JSONObject();
                    try {
                        if (authenticated==null) {
                            jsonResponse.put("message", "There is no account with this email");
                            sendJsonResponse(exchange, 500, jsonResponse.toString());
                        } else {
                            sendForgotPasswordEmail(email, authenticated);
                            jsonResponse.put("message", "An email has been sent with your password!");
                            sendJsonResponse(exchange, 200, jsonResponse.toString());
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }


                } else /*if (path.equals("/newsletter")) {
                    sendNewsletter();
                    String response = "Newsletter sent";
                    sendResponse(exchange, 200, response);
                } else*/ {
                    String response = "Not found";
                    sendResponse(exchange, 404, response);
                }
            } else if(method.equals("OPTIONS")){
                handleOptionsRequest(exchange);
            } else {
                String response = "Method Not Allowed";
                sendResponse(exchange, 405, response);
            }
        }

        private String readFormData(HttpExchange exchange) throws IOException {
            InputStream inputStream = exchange.getRequestBody();
            byte[] buffer = new byte[1024];
            StringBuilder formData = new StringBuilder();

            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                String chunk = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
                formData.append(chunk);
            }

            Map<String, String> formParams = parseFormData(formData.toString());
            for (Map.Entry<String, String> entry : formParams.entrySet()) {
                System.out.println("Parameter: " + entry.getKey() + " = " + entry.getValue());
            }

            return formData.toString();
        }

        private static String extractBoundary(String formData) {
            Pattern pattern = Pattern.compile("^--(.*?)\r\n", Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(formData);

            if (matcher.find()) {
                return matcher.group(1);
            }

            return null;
        }

        private static Map<String, String> parseFormData(String formData, String boundary) {
            Map<String, String> formParams = new HashMap<>();
            Pattern pattern = Pattern.compile(boundary + "\\r\\nContent-Disposition: form-data; name=\"(.*?)\"\r\n\r\n(.*?)\\r\\n", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(formData);

            while (matcher.find()) {
                String key = matcher.group(1);
                String value = matcher.group(2);
                formParams.put(key, value);
            }

            return formParams;
        }

        private Map<String, String> parseFormData(String formData) {
            Map<String, String> formParams = new HashMap<>();
            String[] params = formData.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    String key = keyValue[0];
                    String value = keyValue[1];
                    formParams.put(key, value);
                }
            }
            return formParams;
        }

        private void SimplifyAndSend(HttpExchange exchange, List<Shoe> shoes) throws IOException {
            Collections.shuffle(shoes);

            //List<Shoe> randomShoes = shoes.subList(0, 7);

            List<SimpleShoe> simpleShoes = new ArrayList<>();

            for(Shoe shoe: shoes) {
                simpleShoes.add(new SimpleShoe(shoe));
            }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            String json = gson.toJson(simpleShoes);

            System.out.println("Lista papuci: " + simpleShoes.toString());

            sendJsonResponse(exchange, 200, json);
        }

        private void handleOptionsRequest(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
            exchange.sendResponseHeaders(204, -1);
            exchange.getResponseBody().close();
        }
        private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
            exchange.sendResponseHeaders(statusCode, response.length());
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(response.getBytes());
            outputStream.close();
        }

        private static String extractValue(String requestData, String key) {
            int startIndex = requestData.indexOf(key + "=");
            if (startIndex == -1) {
                return null;
            }
            int endIndex = requestData.indexOf("&", startIndex);
            if (endIndex == -1) {
                endIndex = requestData.length();
            }
            return requestData.substring(startIndex + key.length() + 1, endIndex);
        }

        private void sendJsonResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
            exchange.sendResponseHeaders(statusCode, response.length());
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(response.getBytes());
            outputStream.close();
        }

        private static void sendForgotPasswordEmail(String recipient, User user) {


            String subject = "Password mail";

            String body = "<div style=\"display: flex;\">"
                    + "<div style=\"margin-left: 10px;\">"
                    + "<p>Here is your password:</p>"
                    + "<p><strong>Password: </strong> " + user.getPassword() + "</p>"
                    + "</div>"
                    + "</div>"
                    + "<p>Best regards,<br>Your Shoe Team</p>";

            String fromEmail = "no-reply@shoesteam.com";
            String host = "smtp.gmail.com";
            int port = 587; // SMTP port (typically 25 or 587)

            String username = "shopesassistant@gmail.com";
            String password = "ztbmpaqfjiyygeld";

            Properties properties = new Properties();
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.host", host);
            properties.put("mail.smtp.port", port);

            // Create session with authentication
            Session session = Session.getInstance(properties, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            try {
                // Create the email with the embedded image
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(fromEmail));
                message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
                message.setSubject(subject);

                MimeMultipart multipart = new MimeMultipart("related");

                // Create the HTML body part
                MimeBodyPart htmlPart = new MimeBodyPart();
                htmlPart.setContent(body, "text/html");

                // Add the HTML body part to the multipart container
                multipart.addBodyPart(htmlPart);

                // Set the multipart content as the message content
                message.setContent(multipart);

                // Send the email
                Transport.send(message);

                System.out.println("Password Reset Email sent successfully to: " + recipient);
            } catch (MessagingException e) {
                System.out.println("Failed to send password reset email. Error message: " + e.getMessage());
            }
        }
    }
}
