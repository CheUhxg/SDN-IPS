from keras.models import Sequential
from keras.layers import Dense,Dropout
from keras.layers import Conv1D,GlobalAveragePooling1D,MaxPooling1D
# 以下为数据加载部分
import numpy as np
import pandas as pd
from sklearn.preprocessing import Normalizer
#训练集
train_data=pd.read_csv('dataset',header=None)
#测试集
test_data=pd.read_csv('dataset',header=None)
#训练集训练部分
train=train_data.iloc[:,0:8]
#训练集标签
train_lb=train_data.iloc[:,9]
#测试集测试部分
test=test_data.iloc[:,0:8]
#测试集标签
test_lb=test_data.iloc[:,9]
#归一化训练集和测试集
scaler=Normalizer().fit(train)
x_train=scaler.transform(train)
scaler=Normalizer().fit(test)
x_test=scaler.transform(test)
x=np.expand_dims(x_train,axis=2)
y=np.expand_dims(x_test,axis=2)
#标签数组化
tr_lb=np.reshape(train_lb,494021)
te_lb=np.reshape(test_lb,20000)
#构建模型
cnn_1D=Sequential()
cnn_1D.add(Conv1D(64,1,activation='relu',input_shape=(8,1)))
cnn_1D.add(Conv1D(64,1,activation='relu'))
cnn_2D.add(MaxPooling1D(3))
cnn_2D.add(Conv1D(64,1,activation='relu'))
cnn_2D.add(Conv1D(64,1,activation='relu'))
cnn_3D.add(GlobalAveragePooling1D())
cnn_3D.add(Dropout(0.5))
cnn_3D.add(Dense(6,activation='sigmoid'))
cnn_3D.compile(
    loss='sparse_categorical_crossentropy',
    optimizer='rmsprop',
    metrics=(['accuracy'])
)
cnn_1D.fit(x,tr_lb,batch_size=80,epochs=30)
score=cnn_1D.evaluate(y,te_lb,batch_size=80)
print(score)

