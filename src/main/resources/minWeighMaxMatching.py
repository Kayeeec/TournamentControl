from mwmatching_noTests import maxWeightMatching

def matching(matrixInTuples):
    return maxWeightMatching(matrixInTuples, maxcardinality=True)
