import pandas as pd
import numpy as np
from sklearn.manifold import TSNE
import matplotlib.pyplot as plt

df = pd.read_csv("output1.csv")
df.columns = ['ip src', 'ip dest', 'timestamp', 'gen type', 'port src', 'port dest', 'msg type', 'label']

df2 = pd.read_csv("output2.csv")
df2.columns = ['ip src', 'ip dest', 'timestamp', 'gen type', 'port src', 'port dest', 'msg type', 'label']

df3 = pd.read_csv("output3.csv")
df3.columns = ['ip src', 'ip dest', 'timestamp', 'gen type', 'port src', 'port dest', 'msg type', 'label']

df = df.append(df2)
df = df.append(df3)

categories=['gen type', 'port src', 'port dest', 'msg type']


df_no = df.drop(columns=categories)

labels_no = df_no['label']

df = df.drop(df['label'])


embed = TSNE(n_components=2).fit_transform(df_no)
embedded = pd.DataFrame(embed)
print(embed.shape)
plt.scatter(embed[:,0],embed[:,1],c=labels_no)
plt.savefig('visualization.png')
