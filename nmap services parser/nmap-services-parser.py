# Specify save folder
from pathlib import Path

scriptPath = Path(__file__).resolve()
saveFolder = scriptPath.parent

# Download the nmap-services file
import requests

url: str = 'https://raw.githubusercontent.com/nmap/nmap/master/nmap-services'
response = requests.get(url)

if response.status_code == 200:
    with open(saveFolder / 'nmap-services', 'wb') as file:
        file.write(response.content)
    print("nmap-services file downloaded successfully.")
else:
    print("Failed to download nmap-services file.")

# Parse the nmap-services file
DATA_STARTING_LINE: int = 23

portsTCP: list[str] = []
portsUDP: list[str] = []

with open(saveFolder / 'nmap-services', 'r') as file:
    for i in range(DATA_STARTING_LINE - 1):
        next(file)

    for line in file:
        data: list[str] = line.strip().split()

        if len(data) > 3: 
            data[3] = ' '.join(data[3:])
            data = data[:4]
            data[3] = data[3][2:]

        if len(data) < 4: 
            data.append('')

        if data[1].endswith('/tcp'):
            data[1] = int(data[1][:-4])
            data[2] = float(data[2])
            portsTCP.append(data)
        elif data[1].endswith('/udp'):
            data[1] = int(data[1][:-4])
            data[2] = float(data[2])
            portsUDP.append(data)

print(f'Length: {len(portsTCP)} -> {portsTCP}')
print(f'Length: {len(portsUDP)} -> {portsUDP}')

# Save the nmap-services data into a JSON file
import json

jsonData: dict[str, list[dict]] = {
    'tcp': [],
    'udp': []
}

for port in portsTCP:
    data: dict = {
        'service name': port[0],
        'port': port[1],
        'frequency': port[2],
        'comment': port[3]
    }
    jsonData['tcp'].append(data)

for port in portsUDP:
    data: dict = {
        'service name': port[0],
        'port': port[1],
        'frequency': port[2],
        'comment': port[3]
    }
    jsonData['udp'].append(data)

jsonData['tcp'].sort(key=lambda x: x['frequency'], reverse=True)
jsonData['udp'].sort(key=lambda x: x['frequency'], reverse=True)

with open(saveFolder / 'nmap-services.json', 'w') as jsonFile:
    json.dump(jsonData, jsonFile, indent=4)

print(f'JSON data has been written to {saveFolder / "nmap-services.json"}')