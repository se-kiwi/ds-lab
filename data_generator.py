import random
from random import choice, randint
import json, dataclasses,time
from dataclasses import dataclass

concurrency = ["CNY", "USD", "JPY", "EUR"]
commodity = ["1", "2", "3", "4"]
users = [1, 2, 3, 4, 5, 6, 7]

MAX_ITEM = 5
MAX_PURCHASE = 5
GENERATE_STRIDE = 2 ** 10

@dataclass
class Item:
    id: str
    number: int

class ItemFactory:
    @staticmethod
    def create_random():
        return Item(choice(commodity), randint(1, MAX_ITEM))

    @staticmethod
    def create_random_list(num):
        return [ItemFactory.create_random() for i in range(num)]

@dataclass
class Record:
    user_id: str
    initiator: str
    time: int
    items: list

nowtime = lambda : int(round(time.time() * 1000))

class RecordFactory:
    @staticmethod
    def create_random():
        return Record(choice(users), choice(concurrency),\
            nowtime(), ItemFactory.create_random_list(randint(1, MAX_PURCHASE)))
    
    @staticmethod
    def create_random_list(num):
        return [RecordFactory.create_random() for i in range(num)]

class Generator:
    class EnhancedJSONEncoder(json.JSONEncoder):
        def default(self, o):
            if dataclasses.is_dataclass(o):
                return dataclasses.asdict(o)
            return super().default(o)

    @staticmethod
    def generate(num):
        t = RecordFactory.create_random_list(num)
        ans = ""
        for i in t:
            ans += json.dumps(i, cls=Generator.EnhancedJSONEncoder)
            ans += "\n"
        return ans

    @staticmethod
    def generate_to_file(num, file):
        offset = num % GENERATE_STRIDE
        num = num - offset
        with open(file, "w") as f:
            f.write(Generator.generate(offset))
            for i in range(0, num, GENERATE_STRIDE):
                f.write(Generator.generate(GENERATE_STRIDE))
        

        

if __name__ == "__main__":
    random.seed()
    Generator.generate_to_file(20, "test-1.data")
    
        