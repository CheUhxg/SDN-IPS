from train import *
import PySimpleGUI as sg

sg.theme('Default 1') 
# 设定窗口内容
layout = [  [sg.Text('Payload输入框' , key='Text1',size=(50,2) ,justification='center' )],
            [sg.Text('输入HTTP请求'), sg.Multiline(size=(50,9))],
            [sg.Button('Ok'), sg.Button('Cancel')] ]

# 创建窗口
window = sg.Window('异常流量识别', layout,element_justification='c')
# 设置事件循环，等待输入内容
while True:
    event, values = window.read()
    if event == sg.WIN_CLOSED or event == 'Cancel': 
        break

    a = values[0]
    a = a.strip()
    a = a.replace(" ","")
    a = a.rstrip()
    a = a.replace("\r", "")
    solv = []

    if a == '':
        window.Element('Text1').Update("请输入")
        window.Element('Text1').Update(text_color='gray')
    else:
        solv.append(a)
        solv = calcFeatures(solv,2)
        result = model.predict(scaler.transform(solv))

        if result[0] == 0:
            window.Element('Text1').Update("白流量")
            window.Element('Text1').Update(text_color='green')
        else:
            window.Element('Text1').Update("异常流量")
            window.Element('Text1').Update(text_color='red')

    values[0] = []
    a = ""
    solv = []
    
window.close()