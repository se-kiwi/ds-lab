import requests
from multiprocessing.pool import Pool
from multiprocessing.context import TimeoutError, Process
import time
from data_generator import Generator

# config 
MAX_PROCESS = 5
URL = "http://localhost:8080/"

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
        return requests.post(URL, json=content, headers={'Connection':'close'})
    
    def run(self):
        with open(self.file, "r") as f:
            s = f.readlines()
        self.pool.map(Sender.send, s)

def send_from_file(file):
    Sender(file, MAX_PROCESS).run()
    
if __name__ == "__main__":
    requests.post(URL, json={"user":"data"}, headers={'Connection':'close'})
    filenames = ["test-1.data" , "test-2.data", "test-3.data"]
    for i in filenames:
        Generator.generate_to_file(200, i)
    process = []
    for i in filenames:
        p = Process(target=send_from_file, args=(i,))
        p.start()
        print(str(p.pid) + "is running")
        process.append(p)
    for p in process:
        p.join()
