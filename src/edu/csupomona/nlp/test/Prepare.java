/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.csupomona.nlp.test;

import edu.csupomona.nlp.tool.crawler.Amazon;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Xing
 */
public class Prepare {
    
    public HashMap<String, String> readProducts(String filePath) 
            throws FileNotFoundException, IOException {
        FileReader fr = new FileReader(filePath);
        BufferedReader br = new BufferedReader(fr);
        String line;
        HashMap<String, String> products = new HashMap<>();
        while ((line = br.readLine()) != null) {
            String[] items = line.trim().split("  ");
            if (items[0].length() > 0)
                products.put(items[0], items[1]);
        }
        
        return products;
    }
    
    public static void main(String[] args) throws IOException {
        Prepare prep = new Prepare();
        
        String path = "./data/reviews/";
        
        HashMap<String, String> products = 
                prep.readProducts(path + "product_id.txt");
        
        Amazon am = new Amazon();
        
        for (String productId : products.keySet()) {
            String newFolder = path + products.get(productId) + "/";
            
            new File(newFolder).mkdir();
            
            List<String> reviews = am.crawl(productId);
            Integer idx = 0;
            for (String review : reviews) {
                FileWriter fw = new FileWriter(newFolder + idx.toString() + ".txt");
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(review + "\n");
                bw.close();
                
                idx++;
            }
            
        }
    }
    
}
