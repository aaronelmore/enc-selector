import numpy as np
from time import time
from ndnn.dataset import LSTMDataSet
from ndnn.rnn import LSTMTrainGraph, LSTMPredictGraph


trainds = LSTMDataSet('data/ptb.train.txt')
validds = LSTMDataSet('data/ptb.valid.txt')
testds = LSTMDataSet('data/ptb.test.txt')


hidden_dim = 200
batch_size = 50
parameters = []
model = 'model_LSTM.pkl'
eta = 0.5
decay = 0.9

np.random.seed(0)

param_store = []

def Predict(max_step, prefix):

    predictGraph = LSTMPredictGraph(trainds.num_char(), hidden_dim)
    if len(param_store) > 0:
        predictGraph.load(param_store)

    predictGraph.build(prefix, max_step)

    predictGraph.test()

    idx = [pred.value for pred in predictGraph.predicts]
    stop_idx = trainds.translate_to_num('}')[0]

    if stop_idx in idx:
        return idx[0:idx.index(stop_idx) + 1]
    else:
        return idx

def Eval(ds):

    for batch in ds.batches(batch_size):
        graph = LSTMTrainGraph(trainds.num_char(), hidden_dim)
        if len(param_store) > 0:
            graph.load(param_store)
        graph.build(batch)
        loss, acc = graph.test()

    return loss


############################################### training loop #####################################################

epoch = 30

# initial Perplexity and loss
loss = Eval(validds)
print("Initial: Perplexity: - Avg loss = %0.5f" % (loss))
best_loss = loss
prefix = 'the agreements bring'
generation = Predict(400, trainds.translate_to_num(prefix))
print("Initial generated sentence ")
print (trainds.translate_to_str(generation))

for ep in range(epoch):

    stime = time()

    for batch in trainds.batches(batch_size):
        graph = LSTMTrainGraph(trainds.num_char(), hidden_dim)
        graph.build(batch)
        graph.train()
        

    duration = (time() - stime) / 60.
    
    param_store = graph.dump()
    
    loss = Eval(validds)
    print("Epoch %d: Perplexity: - Avg loss = %0.5f [%.3f mins]" % (ep, loss, duration))

    # generate some text given the prefix and trained model
    prefix = 'the agreements bring'
    generation = Predict(400, trainds.translate_to_num(prefix))
    print("Epoch %d: generated sentence " % ep)
    print (trainds.translate_to_str(generation))