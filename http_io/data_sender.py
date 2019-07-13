import requests
from multiprocessing.pool import Pool
from multiprocessing.context import TimeoutError, Process
import time
from data_generator import Generator
import json
import sys
from functools import partial

# config 
URL = "http://httpserver:30623/"
# URL = "http://202.120.40.8:30623/"

'''
Sender will send the content of file line by line to URL
'''
class Sender:
    def __init__(self, files, proc, ops):
        self.files = files
        self.pool = Pool(processes=proc)
        self.ops = ops
    
    @staticmethod
    def send(ops, file):
        ans = []
        delay = 1.0 / ops
        with open(file, "r") as f:
            s = f.readlines()
            start2 = time.time()
            print("start send")
            for i in s:
                start = time.time()
                json_obj = json.loads(i)
                ans.append(requests.post(URL, json=json_obj,\
                    headers={'Connection':'close'}))
                end = time.time()
                sleep_time = delay - end + start
                # print("sleep time:" + str(sleep_time))
                if (sleep_time > 0):
                    time.sleep(sleep_time)
            end2 = time.time()
            print("end send")
            print("consume " + str(end2 - start2) + " sec")
        return ans
    
    def run(self):
        self.pool.map(partial(Sender.send, self.ops), self.files)
    
if __name__ == "__main__":
    param = sys.argv
    if len(param) != 4 or not param[1].isdigit() \
        or not param[2].isdigit() or not param[3].isdigit():
        print("Usage:\n"
                "\tprocess number\n"
                "\torder number\n "
                "\torder per second\n"
            "example: \./data_sender.py 3 200 20")
        exit()
    sender_num = int(param[1])
    order_num = int(param[2])
    ops = int(param[3])
    # requests.post(URL, json={"user":"data"}, headers={'Connection':'close'})
    # sender_num = 1
    # order_num = 20
    # ops = 10

    # generate test data files
    filenames = ["test-" + str(i) +".data" for i in range(sender_num)]
    for i in filenames:
        Generator.generate_to_file(order_num, i)

    s = Sender(filenames, sender_num, ops)
    s.run()
    
