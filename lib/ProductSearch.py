#!/usr/bin/env python

import urllib2
import re
import sys


class ProductSearch():
    product_pair = dict()

    search_url = "http://www.amazon.com/s/ref=nb_sb_noss_1?url=search-alias%3Daps&field-keywords="


    def readContent(self, url):
        for ntries in range(5):
            try:
                content = urllib2.urlopen(url, timeout=30).read()
                break
            except:
                print "Something wrong with urlopen(). Retrying...", str(ntries+1), "/ 5"

        return content


    def scrape(self, search_str):
        url = self.search_url + search_str
        content = self.readContent(url)
        content = str(content)
        lines = re.findall(r'<a.+?><span class="lrg bold">.+?</span>', content)
        for line in lines:
            match = re.search(r'/dp/([A-Z0-9]+?)"><.+?>(.+?)</span>', line)
            if match:
                self.product_pair.update({match.group(1) : match.group(2)})
            



    def start(self, search_str):
        print "Searching", search_str
        search_str = re.sub(r'[ ]+', '+', search_str)
        self.scrape(search_str)

        return self.product_pair
        


if __name__ == '__main__':
    try :
        search_str = sys.argv[1]
        ps = ProductSearch()
        product_pair = ps.start(search_str)

        #for product in product_pair:
        #    print product, product_pair[product]

    except:
        print "Please specify something to search"
 
