import requests
from multiprocessing.pool import Pool
from multiprocessing.context import TimeoutError
import time

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
    

if __name__ == "__main__":
    s = Sender("test-1.data", MAX_PROCESS)
    # print(s.url)
    s.run()
