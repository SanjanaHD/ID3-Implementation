/***************************************************
 *  Description: Node structure of Decision Tree
 *  Authors:
 *          Kavitha Rajendran - kxr161830
 *          Sanjana           - sxh164030
 ***************************************************/
public class Node {
    String featureName;
    int value;
    Node leftChild;
    Node rightChild;

    public Node() {
        
    }

    public String getFeature() {
        return featureName;
    }
    
    public void setFeature(String attribute) {
        this.featureName=attribute;
    }

    public Node getLeft() {
        return leftChild;
    }

    public void setLeft(Node leftChild) {
        this.leftChild = leftChild;
    }

    public Node getRight() {
        return rightChild;
    }

    public void setRight(Node rightChild) {
        this.rightChild = rightChild;
    }
}
