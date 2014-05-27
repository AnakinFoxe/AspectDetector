#!/usr/bin/env python

import re
import os
import sys
import enchant

from nltk.corpus import stopwords

def _htmldecode(match):
    try:
        code = match.group(1)
        if code <= 126 and code >= 32:
            return unichr(int(code))
        else:
            return ''
    except:
        return ''


class Preprocessor():
    inpath = 'data/'
    outpath = 'processed_data/'
    en_dict = None 
    stop_nltk = stopwords.words('english')
    stop_local = None
    sw_file = 'stopwords.txt'

    def __init__(self):
        enchant.set_param('enchant.myspell.dictionary.path', '/home/xing/.virtualenvs/python2/lib/python2.7/site-packages/enchant/share/enchant/myspell')
        self.en_dict = enchant.Dict('en_US')
        try:
            swf = open(self.sw_file, 'r')
            tmp = []
            for word in swf:
                tmp.append(word.strip())
            self.stop_local = set(tmp)
        except IOError:
            print "Failed to open", sw_file
        except:
            print "Unexpected error:", sys.exc_info()[0]
            raise


    def removeHtmlTag(self, line):
        parsed = re.sub(r'<[\w\W]+?>', ' ', line)
        return parsed

    def removeUrl(self, line):
        parsed = re.sub(r'(http[s]?://[\w\d\./]+)', '', line)
        return parsed

    def removeNonEnglish(self, line):
        count_en = 0.1
        count_nen = 0.0
        for phrase in line.split():
            for word in re.findall(r'([\w]+)', phrase):
                try:
                    if self.en_dict.check(word):
                        count_en += 1.0
                    else:
                        count_nen += 1.0
                except:
                    count_nen += 1.0
        if count_nen / count_en > 2.0:
            parse = ''
        else:
            parse = line
        return parse

    def removePunctuation(self, line):
        parsed = re.sub(r'([0-9]+)"', r'\1' + ' inch', line)
        parsed = re.sub(r'"', '', parsed)
        parsed = re.sub(r'\*', '', parsed)
        parsed = re.sub(r'\$([0-9]+)', r'\1' + ' dollars', parsed)
        parsed = re.sub(r' @ ', ' at ', parsed)
        return parsed

    def stopword(self, match):
        try:
            word = match.group(1)
            wordl = word.lower()
            if wordl in self.stop_nltk or wordl in self.stop_local:
                return ''
            else:
                return word
        except:
            return word

    def removeStopword(self, line):
        parsed = re.sub(r'([\w\']+)', self.stopword, line)
        return parsed


    def replaceHtmlCode(self, line):
        parsed = re.sub(r'&#([0-9]+)', _htmldecode, line)
        return parsed

    def do(self):
        for filename in os.listdir(self.inpath):
            if filename.endswith('.txt'):
                infile = self.inpath + filename
                outfile = self.outpath + filename

                try:
                    inf = open(infile, 'r')
                    outf = open(outfile, 'w')

                    print "Processing:", infile
                except IOError:
                    print "Failed to open", filename
                except:
                    print "Unexpected error:", sys.exc_info()[0]
                    raise

                for line in inf:
                    parsed = self.removeHtmlTag(line)
                    parsed = self.removeUrl(parsed)
                    parsed = self.removeNonEnglish(parsed)
                    parsed = self.replaceHtmlCode(parsed)
                    parsed = self.removePunctuation(parsed)
                    #parsed = self.removeStopword(parsed)

                    outf.write(parsed)
                outf.flush()
                outf.close()
        print "Done. Find processed file in", self.outpath


if __name__ == '__main__':
    preprocessor = Preprocessor()
    preprocessor.do()

