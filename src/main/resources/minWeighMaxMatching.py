from mwmatching import maxWeightMatching

def matching(matrixInTuples):
    return maxWeightMatching(matrixInTuples, maxcardinality=True)
