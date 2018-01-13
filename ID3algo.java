/*********************************************************************************************
 *  Description: Building Decision Tree using ID3 algorithm & pruning to increase the accuracy
 *  Authors:
 *          Kavitha Rajendran - kxr161830
 *          Sanjana           - sxh164030
 *********************************************************************************************/

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

//temporary place holder for data during partition
class DividedData {
	int featureNo;
	ArrayList<ArrayList<Integer>> leftData;
	ArrayList<ArrayList<Integer>> rightData;
	int label;
	double entropy;
	DividedData(int featureNo, ArrayList<ArrayList<Integer>> leftData, ArrayList<ArrayList<Integer>> rightData,
			int label, double entropy) {
		this.featureNo = featureNo;
		this.leftData = leftData;
		this.rightData = rightData;
		this.label = label;
	}
}

//Implementing ID3 algorithm
public class ID3algo{
	File trainingDataFile;
	File testDataFile;
	double pruningFactor;
    String[] featureList;
    ArrayList<ArrayList<Integer>> data = new ArrayList<ArrayList<Integer>>();
    Node decisionTreeRoot;
	int[] applicableFeatureList;
	int noOfApplicableFeatures;
	int noOfNodes = 0;
	int noOfLeafNodes = 0;
	int noOfTestFeatures;
	int testDataSize;
	int totalNoOfPruning =0;
	//Constructor
	ID3algo(File trainingDataFile, File testDataFile, double pruningFactor){
		this.trainingDataFile = trainingDataFile;
		this.testDataFile = testDataFile;
		this.pruningFactor = pruningFactor;
		decisionTreeRoot = new Node();
		
	}
	
    //Parsing training examples 
    public void parseTrainingData() {
        BufferedReader br = null;
        try {
            String line;
            //Reading file line by line
            br = new BufferedReader(new FileReader(trainingDataFile));
            line = br.readLine();
            //Get the attribute names (split by comma)
            featureList = line.split("\\,+");
            int length = featureList.length;
            while ((line = br.readLine()) != null) {
                ArrayList<Integer> temp = new ArrayList<Integer>();
                String[] values = line.split("\\,+");
                for (int j = 0; j < length; j++) {
                    temp.add(Integer.parseInt(values[j]));
                }
                data.add(temp);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //Close the BufferedReader
            try {
                if (br != null)
                    br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
		applicableFeatureList = new int[featureList.length - 1];
		noOfApplicableFeatures = featureList.length - 1;
    }
	
    //Calculating Log value
	public double findLogBase2(double var){
    	if(var==0)
			return 0;
    	return (Math.log(var) / Math.log(2));
    }
	
	//Find entropy of given data
	private double findEntropy(ArrayList<ArrayList<Integer>> data) {
		double positiveCondition = 0, negativeCondition = 0;
		if (data.size() != 0) {
			int length = data.get(0).size();
			for (int i = 0; i < data.size(); i++) {
				if (data.get(i).get(length - 1) == 0)
					negativeCondition++;
				else
					positiveCondition++;
			}
		}
		double total = positiveCondition + negativeCondition;
		//System.out.println(((-(positiveCondition / total) * findLogBase2((positiveCondition / total)))) - ((negativeCondition / total) * findLogBase2(negativeCondition / total)));
		return ((-(positiveCondition / total) * findLogBase2((positiveCondition / total)))) - ((negativeCondition / total) * findLogBase2(negativeCondition / total));
	}

	//Find value of a particular feature
	public int findFeatureValue(ArrayList<ArrayList<Integer>> data) {
		if (data.size() != 0) {
			int j = data.get(0).size() - 1;
			int sum = 0;
			for (ArrayList<Integer> x : data) {
				sum += x.get(j);
			}
			if (sum > (data.size() / 2))
				return 1;
			else
				return 0;
		}
		return 0;
	}
	
	//Find output value
	static int findLabel(ArrayList<ArrayList<Integer>> data) {
		int noOfZeros = 0, noOfOnes = 0;
		if (data.size() != 0) {
			int index = data.get(0).size() - 1;
			for (ArrayList<Integer> i : data) {
				if (i.get(index) == 0)
					noOfZeros++;
				else
					noOfOnes++;
			}
		}
		if (noOfZeros > noOfOnes)
			return 0;
		else
			return 1;
	}

	//Find weighted entropy of given data split
	public double findWeightedEntropy(ArrayList<ArrayList<Integer>> leftData, ArrayList<ArrayList<Integer>> rightData) {
		double leftEntropy = 0;
		double rightEntropy = 0;
		if (leftData.size() != 0)
			leftEntropy = findEntropy(leftData);
		if (rightData.size() != 0)
			rightEntropy = findEntropy(rightData);
		try {
			double totalEntropy = (double) leftData.size() / (leftData.size() + rightData.size()) * leftEntropy
					+ rightData.size() / (leftData.size() + rightData.size()) * rightEntropy;
			return totalEntropy;
		} catch (ArithmeticException e) {

		}
		return 0;
	}
	
	//populate temp nodes as per the label - 0 or 1
	private void classifyData(ArrayList<ArrayList<Integer>> leftData, ArrayList<ArrayList<Integer>> rightData,
			ArrayList<ArrayList<Integer>> data, int attribute) {
		if (data.size() != 0) {
			for (int i = 0; i < data.size(); i++) {
				//if 0 - add to left child;  1 - add to right child
				if (data.get(i).get(attribute) == 1)
					rightData.add(data.get(i));
				else
					leftData.add(data.get(i));
			}
		}
	}
	
	// Feature with minimum weighted Entropy will have maximum information gain
    //Find a feature with minimum entropy
	private DividedData findFeatureWithMinWeightedEntropy(final ArrayList<ArrayList<Integer>> data, int[] applicableFeatureList) {
		double minEntropy = Double.MAX_VALUE;
		ArrayList<ArrayList<Integer>> tempLeftData = null;
		ArrayList<ArrayList<Integer>> tempRightData = null;
		ArrayList<ArrayList<Integer>> leftData = null;
		ArrayList<ArrayList<Integer>> rightData = null;
		int minAttribute = -1;
		for (int i = 0; i < applicableFeatureList.length; i++) {
			tempLeftData = new ArrayList<ArrayList<Integer>>();
			tempRightData = new ArrayList<ArrayList<Integer>>();
			if (applicableFeatureList[i] == 1)
				//This attribute is already considered; so skipping it
				continue;
			
			//populate temp nodes as per the label - 0 or 1
			classifyData(tempLeftData, tempRightData, data, i);
			
			double totalEntropy = findWeightedEntropy(tempLeftData, tempRightData);
			if (totalEntropy < minEntropy) {
				leftData = tempLeftData;
				rightData = tempRightData;
				minEntropy = totalEntropy;
				minAttribute = i;
			}
		}
		int value = findFeatureValue(data);
		//System.out.println("minAttribute"+minAttribute);
		//System.out.println("minEntropy"+minEntropy);
		DividedData tempNode = new DividedData(minAttribute,leftData, rightData, value, minEntropy);
		return tempNode;
	}
	
	//Recursive call to build decision tree
	private void recCall(Node root, ArrayList<ArrayList<Integer>> data, int[] applicableFeatureList, int noOfApplicableFeatures) {
		if ((noOfApplicableFeatures != 0) && findEntropy(data) != 0) {
			noOfNodes++;
			DividedData tempNode = findFeatureWithMinWeightedEntropy(data, applicableFeatureList);
			root.setFeature(featureList[tempNode.featureNo]);
			noOfApplicableFeatures--;
			applicableFeatureList[tempNode.featureNo] = 1;
			root.leftChild = new Node();
			root.rightChild = new Node();
			root.value = tempNode.label;
			recCall(root.leftChild, tempNode.leftData, applicableFeatureList, noOfApplicableFeatures);
			recCall(root.rightChild, tempNode.rightData, applicableFeatureList, noOfApplicableFeatures);
			applicableFeatureList[tempNode.featureNo] = 0;
		} else {
			root.value = findLabel(data);
		}
	}
	
	//Printing decision tree
	public void printDecisionTree(int level, Node node) {
		if (node.featureName != null) {
			if (node.leftChild.featureName == null && node.rightChild.featureName == null)
				noOfLeafNodes++;
			for (int i = 0; i < level; i++)
				System.out.print("| ");
			System.out.print(node.featureName + ": 0");
			if (node.leftChild.featureName == null) {
				System.out.print(" :" + node.leftChild.value);
			}
			System.out.println();
			printDecisionTree(level + 1, node.leftChild);
			for (int i = 0; i < level; i++)
				System.out.print("| ");
			System.out.print(node.featureName + ": 1");
			if (node.rightChild.featureName == null) {
				System.out.print(" :" + node.rightChild.value);
			}
			System.out.println();
			printDecisionTree(level + 1, node.rightChild);
		}
	}
	//Parsing test data file & validating decision tree
	public double validateTestData(File testFile, Node decisionTreeRoot) {
		//Parsing test data
		String[] testAttributes = null;
		ArrayList<ArrayList<Integer>> testData = new ArrayList<ArrayList<Integer>>();
		BufferedReader br = null;
		try {
			String line;
			br = new BufferedReader(new FileReader(testFile));
			line = br.readLine();
			testAttributes = line.split("\\,+");
			int length = testAttributes.length;
			while ((line = br.readLine()) != null) {
				ArrayList<Integer> temp = new ArrayList<Integer>();
				String[] values = line.split("\\,+");
				for (int j = 0; j < length; j++) {
					temp.add(Integer.parseInt(values[j]));
				}
				testData.add(temp);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		//Count number of test instances predicted correctly
		double correct = 0.0;
		for (ArrayList<Integer> i : testData) {
			if (validate(i, decisionTreeRoot))
				correct = correct + 1;
		}
		//Calculate the accuracy
		noOfTestFeatures = testAttributes.length;
		testDataSize = testData.size();
		double testAccuracy = (correct / testData.size()) * 100.0;
		System.out.println("Number of testing examples : " + testDataSize);
		System.out.println("Number of test features : " + noOfTestFeatures);
		return testAccuracy;
	}

	//If the test data matches the predicted value, return true; else false
	private boolean validate(ArrayList<Integer> i, Node currentNode) {
		if (currentNode.featureName == null) {
			if (currentNode.value == i.get(i.size() - 1)) {
				return true;
			} else
				return false;
		}
		int index = getFeatureIndex(currentNode.featureName);
		int val = i.get(index);
		if (val == 0) {
			if (validate(i, currentNode.leftChild))
				return true;
		} else if (val == 1) {
			if (validate(i, currentNode.rightChild))
				return true;
		} else {
			System.out.println("Unable to find attribute name in file");
			return false;
		}
		return false;
	}

	//helper function to get feature index
	private int getFeatureIndex(String featureName) {
		for (int i = 0; i < featureList.length; i++)
			if (featureList[i].equals(featureName)) {
				return i;
			}
		return -1;
	}
	
	//Building decisiontree as per ID3 
	public void buildDecisionTreeRecursively(){
		recCall(decisionTreeRoot, data, applicableFeatureList, noOfApplicableFeatures);
		System.out.println("Decision Tree before pruning");
		printDecisionTree(0,decisionTreeRoot);

		System.out.println("Number of training examples : " + data.size());
		System.out.println("Number of training features : " + (featureList.length - 1));
		//System.out.println("Total number of nodes in the tree : " +noOfNodes );
		//System.out.println("Number of leaf nodes : " + noOfLeafNodes);
		System.out.println("accuracy of the model on the training data-set before pruning: " + validateTestData(trainingDataFile,decisionTreeRoot));
		
		//Check test data
        double acc = validateTestData(testDataFile,decisionTreeRoot);
		System.out.println("Accuracy of the model on the test data set before pruning: " + acc);
	}
	
	//Clone the tree before pruning
	Node cloneTree(Node decisionTreeRoot) {
        Node newNode = new Node();
        newNode.featureName = decisionTreeRoot.featureName;
		newNode.value = decisionTreeRoot.value;
		newNode.leftChild = decisionTreeRoot.leftChild;
		newNode.rightChild = decisionTreeRoot.rightChild;
        cloneTree(decisionTreeRoot, newNode);
        return newNode;
}
    //Clone recursively
    void cloneTree(Node root, Node newNode) {
        if (root == null) {
            return;
        }
        if (root.leftChild != null) {
            newNode.leftChild = new Node();
            newNode.leftChild.value = root.leftChild.value;
			newNode.leftChild.featureName = root.leftChild.featureName;
			newNode.leftChild.leftChild = root.leftChild.leftChild;
			newNode.leftChild.rightChild = root.leftChild.rightChild;
            cloneTree(root.leftChild, newNode.leftChild);
        }
        if (root.rightChild != null) {
            newNode.rightChild = new Node();
            newNode.rightChild.value = root.rightChild.value;
			newNode.rightChild.featureName = root.rightChild.featureName;
			newNode.rightChild.leftChild = root.rightChild.leftChild;
			newNode.rightChild.rightChild = root.rightChild.rightChild;
            cloneTree(root.rightChild, newNode.rightChild);
        }
    }
    
	//Prune the tree 
	public void pruning(double noOfNodesToBeDeleted) {
		totalNoOfPruning++;
		System.out.println("totalNoOfPruning:"+totalNoOfPruning);
		//Check test data
		System.out.println("\n \n \n Pre-Pruned accuracy\n");
        double accuracyBefore = validateTestData(testDataFile,decisionTreeRoot);
		System.out.println("Accuracy of the model on the test data set before pruning: " + accuracyBefore);
		//clone decision tree & prune the clone
		Node newTree = cloneTree(decisionTreeRoot);
		int tempTreeNoOfNodes = noOfNodes;
		int tempTreeNoOfLeafNodes = noOfLeafNodes;
		for (double i = 0; i < noOfNodesToBeDeleted; i++) {
			Node temp = newTree;
			Node parent = null;
			//Till not leaf node
			int direction;
			while (!(temp.leftChild.featureName == null && temp.rightChild.featureName == null)) {
				if (Math.random() < 0.5)
                    direction = 0;
				else
					direction = 1;
				parent = temp;
				if (direction == 0) {
					if (temp.leftChild.featureName != null)
						temp = temp.leftChild;
					else
						temp = temp.rightChild;
				} else {
					if (temp.rightChild.featureName != null)
						temp = temp.rightChild;
					else
						temp = temp.leftChild;
				}
			}
			temp.featureName = null;
			tempTreeNoOfNodes--;
			tempTreeNoOfLeafNodes--;
			if (parent.leftChild.featureName == null && parent.rightChild.featureName == null) {
				tempTreeNoOfLeafNodes++;
			}
		}
		System.out.println("Decision Tree after pruning");
		printDecisionTree(0,newTree);
		System.out.println("\n \n \n Post-Pruned accuracy\n");
		System.out.println("Number of training examples : " + data.size());
		System.out.println("Number of training features : " + (featureList.length - 1));
		//System.out.println("Total number of nodes in the tree : " +tempTreeNoOfNodes );
		//System.out.println("Number of leaf nodes : " + tempTreeNoOfLeafNodes);
		System.out.println("accuracy of the model on the training data-set after pruning: " + validateTestData(trainingDataFile, newTree));
		
		//Check test data
        double accuracyAfter = validateTestData(testDataFile,newTree);
		System.out.println("Accuracy of the model on the test data set after pruning: " + accuracyAfter);
		
		//If accuracy not improving, prune again - upto 50 times
		if(accuracyAfter<accuracyBefore & totalNoOfPruning<=50){
			System.out.println("Accuracy of the model on the test data set before pruning: " + accuracyBefore);
		    //System.out.println("Accuracy of the model on the test data set after pruning: " + accuracyAfter);
			System.out.println("Going for different way of pruning");
			pruning(noOfNodesToBeDeleted);
		}
		noOfNodes=tempTreeNoOfNodes;
		noOfLeafNodes=tempTreeNoOfLeafNodes;
	}
	
	//Main method
	public static void main(String args[]) {
		//cmd line argument handling
		File trainingDataFile = new File(args[0]);
		File testDataFile = new File(args[1]);
		double pruningFactor = Double.parseDouble(args[2]);
		
		//constructor call
		ID3algo id3 = new ID3algo(trainingDataFile, testDataFile, pruningFactor);
		
		//Parse input & place all date in root node
		id3.parseTrainingData();
		
		//Build decision Tree & validate test data
		id3.buildDecisionTreeRecursively();
		
		//prune as per pruning factor
		double nodesToBeRemoved = pruningFactor * (id3.noOfNodes);
		id3.pruning(nodesToBeRemoved);
	}
} 
