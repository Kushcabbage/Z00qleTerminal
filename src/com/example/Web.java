package com.example;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;


public class Web {

    String content = null;
    URLConnection connection = null;

    public void GetPage(String url) {
        try {
            connection = new URL(url).openConnection();
            String redirect = connection.getHeaderField("Location");
            if (redirect != null){
                connection = new URL(redirect).openConnection();
            }
            Scanner scanner = new Scanner(connection.getInputStream());
            scanner.useDelimiter("\\Z");
            content = scanner.next();
            //System.out.println(content);

            PrintWriter out = new PrintWriter("last,web.getpage.html");
            out.println(content);
            out.close();



        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void testpage(String uri){
        URL url;
        InputStream is = null;
        BufferedReader br;
        String line;

        try {
            url = new URL(uri);
            is = url.openStream();  // throws an IOException
            br = new BufferedReader(new InputStreamReader(is));

            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (MalformedURLException mue) {
            mue.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ioe) {
                // nothing to see here
            }
        }

    }

    public result[] search(String url,String term,WebDriver driver){
        System.out.println("seaching for "+term);



            driver.get(url);

        // Find the text input element by its name
            WebElement EmailElement = driver.findElement(By.name("q"));
            // Enter something to search for
            EmailElement.sendKeys(term);

            // Now submit the form. WebDriver will find the form for us from the element
            EmailElement.submit();

            // Check the title of the page
            System.out.println("Page title is: " + driver.getTitle());

            if(driver.getTitle().contains("\"\" - ")){           /////search is empty :(
                return null;
            }





            WebElement resultTable = driver.findElement(By.className("table-torrents"));
            List<WebElement> allRows = resultTable.findElements(By.tagName("tr"));

            List<result> results= new ArrayList<>();
            int errcount=0;
            for (WebElement row : allRows) {
                List<WebElement> cells = row.findElements(By.tagName("td"));
                if(cells.size()==6) {
                        List<WebElement>links= row.findElements(By.tagName("a"));
                        String maglink="";
                        for(WebElement link:links){
                            if(link.getAttribute("href").contains("magnet:?")){
                                maglink=link.getAttribute("href");
                            }
                        }
                    try {
                        int pos = Integer.parseInt(cells.get(0).getText().replace(".", ""));
                        String name = cells.get(1).getText().split("\n")[0];
                        String qual = cells.get(1).getText().split("\n")[1];
                        String sound="";
                        String video="";
                        Pattern p = Pattern.compile("\\w {3}\\w");
                        Matcher m = p.matcher(qual);
                        if(m.find()) {
                             sound = qual.split("   ")[0];
                             video = qual.split("   ")[1];
                        }
                        else{
                             sound="";
                             video="";
                        }
                        String size = cells.get(3).getText().replace(" ","");
                        String age = cells.get(4).getText();
                        String seed = cells.get(5).getText().split("\n")[0];
                        String leech = cells.get(5).getText().split("\n")[1];               ///throws error
                        results.add(new result(maglink,pos, name, sound, video, size, age, seed, leech));

                    }catch (Exception e){
                        errcount++;
                        //e.printStackTrace();
                    }
                }

            }
            System.out.println("errcount="+errcount);
            System.out.println("Got "+results.size()+" results");
            System.out.print(Main.ANSI_BLACK);
            int count=1;
            for(result res: results){
                System.out.print(Main.ANSI_WHITE+fixedpos(count)+".");
                System.out.println(res.toString());
                count++;
            }
            result[] resultarray = new result[results.size()+1];

            //check for suggested media

        try {
            WebElement suglist = driver.findElement(By.className("suglist"));
            List<WebElement> suglinks = suglist.findElements(By.tagName("li"));
            int sugnum=0;
            for(WebElement link: suglinks){


                //remove punctuation
                if(link.getAttribute("title").replaceAll("\\p{P}", "").toLowerCase().equals(term.trim().toLowerCase())){


                    WebElement typebox = link.findElement(By.className("zqf-small"));
                    String type="";
                    if(typebox.toString().contains("tv")){
                        type="tv";
                        System.out.print("FOUND MATCHING TV SERIES");
                    }
                    if(typebox.toString().contains("movies")){
                        type="movies";
                        System.out.print("FOUND MATCHING MOVIE");
                    }


                    System.out.println(" Go to? (y/n)");

                    Main.SetMatch(true);

                    //bump array and add link to result 0
                    List<WebElement> links = suglist.findElements(By.tagName("a"));
                    resultarray[0] = new result(links.get(sugnum).getAttribute("href"),0,"match","","","","","","");

                }
                sugnum++;
            }
        }catch(Exception e){
            //e.printStackTrace();
        }






        System.out.println("Enter a number to dl or press q to return to search");
        Main.context="results";


            int i=1;
            for(result res:results){
                resultarray[i]=res;
                i++;
            }
            return resultarray;



    }

    public String fixedpos(int  pos){

        if(pos<10){
            return " "+String.valueOf(pos);
        }
        return String.valueOf(pos);
    }

    public static result[] getmatchpage(String url,WebDriver driver){
        driver.get(url);

        /////For film
        try {
            WebElement table = driver.findElement(By.className("table-torrents"));
            List<WebElement> allRows = table.findElements(By.tagName("tr"));
            List<result> results = new ArrayList<>();
            for (WebElement row : allRows) {
                //System.out.println(row.getText());
                if(row.getText().equals("")){continue;}
                result tmp=TableRowToResult(row);
                results.add(tmp);
                try {
                    System.out.println(tmp.toString());
                }catch (Exception e){
                    System.out.println(row.getText().replace("\n",""));
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        ///for TV??

        return new result[2];
    }

    public static result TableRowToResult(WebElement row){
        List<WebElement> cells = row.findElements(By.tagName("td"));

        if(cells.size()==5){    //movies
            String zero = cells.get(0).getText();
            String name = cells.get(1).getText();
            String sound="";
            Pattern p = Pattern.compile("\\w\\n\\w");
            Matcher m = p.matcher(name);
            if(m.find()) {
                sound = name.split("\n")[1];
                name=name.split("\n")[0];

            }
            String size = cells.get(2).getText();
            String age= cells.get(3).getText();
            String[] seedleech = cells.get(4).getText().split("\n");
            String seed=seedleech[0];
            String leech = seedleech[1];
            return new result("",0, name, sound, "", size, age, seed, leech);

        }

        if(cells.size()==4) {       ///TV

            String zero = cells.get(0).getText();
            String one = cells.get(1).getText();
            String two = cells.get(2).getText();
            String three= cells.get(3).getText();
            List<WebElement>links= row.findElements(By.tagName("a"));
            String maglink="";
            for(WebElement cell: cells){
                System.out.println(cell.getText().replace("\n",""));
            }

            for(WebElement link:links){
                if(link.getAttribute("href").contains("magnet:?")){
                    maglink=link.getAttribute("href");
                }
            }
            try {

                String name = cells.get(1).getText().split("\n")[0];
                String qual = cells.get(1).getText().split("\n")[1];
                String sound="";
                String video="";
                Pattern p = Pattern.compile("\\w {3}\\w");
                Matcher m = p.matcher(qual);
                if(m.find()) {
                    sound = qual.split("   ")[0];
                    video = qual.split("   ")[1];
                }
                else{
                    sound="";
                    video="";
                }
                String size = cells.get(3).getText().replace(" ","");
                String age = cells.get(4).getText();
                String seed = cells.get(5).getText().split("\n")[0];
                String leech = cells.get(5).getText().split("\n")[1];               ///throws error

                return new result(maglink,0, name, sound, video, size, age, seed, leech);
            }catch (Exception e){

            }

        }
        System.out.println("row didn't parse");
        return null;
    }

}