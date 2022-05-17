import requests
import json

headers = {
        "Accept": "application/json"
}

def getData():
    with open('result.json', 'r') as fd :
      data = fd.readlines()

def main():
    data = getData()
    url = 'http://211.83.110.7:5000/detectresult'
    res = requests.post(url=url, headers=headers, data=json.dumps(data))
    print(res.content)

if __name__ == '__main__':
    main()