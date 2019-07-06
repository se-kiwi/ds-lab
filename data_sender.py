import requests
from multiprocessing.pool import Pool
from multiprocessing.context import TimeoutError
import time


MAX_PROCESS = 5
URL = "http://localhost:8080"

cnt = 0
class Sender:
    def __init__(self, url, file, proc):
        self.url = url
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
    s = Sender(URL, "test-1.data", MAX_PROCESS)
    s.run()
    # r = requests.post(URL, json="{}", headers={'Connection':'close'})
    # time.sleep(1)
    # print(r)
