#!/usr/bin/env python

from amazonproduct import API
import urllib2
import re


class ReviewCrawler():
    filename = "product_id.txt"
    inf = None
    product_pair = dict()

    api=API(locale='us')
    seeall_rest = "/ref=cm_cr_if_all_link?ie=UTF8&linkCode=xm2&showViewpoints=1&sortBy=bySubmissionDateDescending&tag=tweets082-20"
    next_rest1 = "/ref=cm_cr_pr_top_link_next_"
    next_rest2 = "?ie=UTF8&pageNumber="
    next_rest3 = "&showViewpoints=0&sortBy=bySubmissionDateDescending"



    def __init__(self):
        try:
            self.inf = open(self.filename, 'r')
        except IOError:
            print "Can not open ", self.filename
        except:
            print "Unexpected error:", sys.exc_info()[0]
            raise

    def parseProductId(self):
        for line in self.inf:
            self.product_pair.update({line.split()[0] : line.split()[1]})


    def getReviewUrl(self, product_id):
        response = self.api.item_lookup(product_id, ResponseGroup='Reviews')
        url = response.Items.Item.CustomerReviews.IFrameURL
        return str(url)


    def readContent(self, url):
        for ntries in range(5):
            try:
                content = urllib2.urlopen(url, timeout=30).read()
                break
            except:
                print "Something wrong with urlopen(). Retrying...", str(ntries+1), "/ 5"

        return content


    def scrapeReview(self, url, outf):
        content = self.readContent(url)
        content = str(content) [::-1]
        main_url = ''
        match = re.search(r'(>a/<\.\.\.sweiver [\w\W]*?a<)', content)
        if match:
            main_url = match.group(1)
            main_url = main_url [::-1]
            match = re.search(r'"([\w\W]+?)"', main_url)
            if match:
                main_url = match.group(1)
                seeall_url = main_url + self.seeall_rest 
                page = 1
                content = self.readContent(seeall_url)
                while (True):
                    n_reviews = 0
                    reviews = re.findall(r'<div class="reviewText">([\w\W]*?)</div>', content)
                    print "Parsing Page " + str(page) + "..."
                    for review in reviews:
                        outf.write(str(review) + "\n")
                        n_reviews = n_reviews + 1
                    outf.flush()
                    if (n_reviews == 0):
                        print "Done Parsing at Page" + str(page)
                        break;
                    page = page + 1
                    next_url = main_url + self.next_rest1 + str(page) + self.next_rest2 + str(page) + self.next_rest3
                    content = self.readContent(next_url)

                outf.close()

            else:
                print "no url"
        else:
            print "no match"


    def scrape(self, product_id, product_name):
        try:
            outf = open('data/' + product_name + '.txt', 'w')
        except IOError:
            print "Can not create ", product_name, ".txt"
        except:
            print "Unexpected error:", sys.exc_info()[0]
            raise

        url = self.getReviewUrl(product_id)
        self.scrapeReview(url, outf)
        


    def start(self):
        self.__init__()
        self.parseProductId()
        
        num = 1
        total = len(self.product_pair.keys())

        for product_id in self.product_pair.keys():
            product_name = self.product_pair[product_id]

            print  "Scraping:", str(num), "/", str(total), ":", product_name
            num += 1

            self.scrape(product_id, product_name)


if __name__ == '__main__':
    crawler = ReviewCrawler()
    crawler.start()
