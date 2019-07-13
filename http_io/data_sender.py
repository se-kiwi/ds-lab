import requests
from multiprocessing.pool import Pool
from multiprocessing.context import TimeoutError, Process
import time
from data_generator import Generator
import json
import sys

# config 
MAX_PROCESS = 5
# URL = "http://localhost:30623/"
URL = "http://202.120.40.8:30623/"

'''
Sender will send the content of file line by line to URL
'''
class Sender:
    url : str
    def __init__(self, file, proc = MAX_PROCESS):
        self.file = file
        self.pool = Pool(processes=proc)
    
    @staticmethod
    def send(content):
        json_obj = json.loads(content)
        return requests.post(URL, json=json_obj, headers={'Connection':'close'})
    
    def run(self):
        with open(self.file, "r") as f:
            s = f.readlines()
        self.pool.map(Sender.send, s)

def send_from_file(file):
    Sender(file, MAX_PROCESS).run()
    
if __name__ == "__main__":
    param = sys.argv
    if len(param) != 3 and not param[1].isdigit() and not param[1].isdigit():
        print("Usage:\n\tprocess number\n\torder number\nexample: ./data_sender.py 3 200")
        exit()
    sender_num = int(param[1])
    order_num = int(param[2])
    requests.post(URL, json={"user":"data"}, headers={'Connection':'close'})
    filenames = ["test-" + str(i) +".data" for i in range(sender_num)]
    for i in filenames:
        Generator.generate_to_file(order_num, i)
    process = []
    for i in filenames:
        p = Process(target=send_from_file, args=(i,))
        p.start()
        print(str(p.pid) + "is running")
        process.append(p)
    for p in process:
        p.join()
