#!/usr/bin/env python

import urllib2
import re
import sys

from Preprocessor import Preprocessor


class SimpleCrawler():
    product_review = ""

    proc = Preprocessor()
    main_url = "http://www.amazon.com/product-reviews/"
    seeall_rest = "/ref=cm_cr_if_all_link?ie=UTF8&linkCode=xm2&showViewpoints=1&sortBy=bySubmissionDateDescending&tag=tweets082-20"
    next_rest1 = "/ref=cm_cr_pr_top_link_next_"
    next_rest2 = "?ie=UTF8&pageNumber="
    next_rest3 = "&showViewpoints=0&sortBy=bySubmissionDateDescending"


    def getReviewUrl(self, product_id):
        return self.main_url + product_id


    def readContent(self, url):
        for ntries in range(5):
            try:
                content = urllib2.urlopen(url, timeout=30).read()
                break
            except:
                print "Something wrong with urlopen(). Retrying...", str(ntries+1), "/ 5"

        return content


    def scrapeReview(self, url):
        content = self.readContent(url)
        content = str(content) #[::-1]
        page = 1
        while True:
            n_reviews = 0
            reviews = re.findall(r'<div class="reviewText">([\w\W]*?)</div>', content)
            print "Parsing Page " + str(page) + "..."
            for review in reviews:
                self.product_review += self.preprocess(str(review) + "\n")
                n_reviews = n_reviews + 1
            if (n_reviews == 0):
                print "Done Parsing at Page" + str(page)
                break;
            page = page + 1
            next_url = url + self.next_rest1 + str(page) + self.next_rest2 + str(page) + self.next_rest3
            content = str(self.readContent(next_url))

    def preprocess(self, line):
        parsed = self.proc.removeHtmlTag(line)
        parsed = self.proc.removeUrl(parsed)
        parsed = self.proc.removeNonEnglish(parsed)
        parsed = self.proc.replaceHtmlCode(parsed)
        parsed = self.proc.removePunctuation(parsed)
        parsed = self.proc.removeStopword(parsed)
        return parsed


    def start(self, product_id):
        print "Scraping product_id =", product_id
        url = self.getReviewUrl(product_id)
        self.scrapeReview(url)
        
        return self.product_review


if __name__ == '__main__':
    try :
        product_id = sys.argv[1]
        crawler = SimpleCrawler()
        product_review_txt = crawler.start(product_id)

        #print product_review_txt
    except:
        print "Please specify a product id"
 
