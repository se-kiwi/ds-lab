import requests
from multiprocessing.pool import Pool
from multiprocessing.context import TimeoutError
import time
import json

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
        # print(content['user_id'])
        # print(json_obj['user_id'])
        return requests.post(URL, json=json_obj, headers={'Connection':'close'})
    
    def run(self):
        with open(self.file, "r") as f:
            s = f.readlines()
        self.pool.map(Sender.send, s)
    

if __name__ == "__main__":
    s = Sender("test-1.data", MAX_PROCESS)
    # print(s.url)
    s.run()
