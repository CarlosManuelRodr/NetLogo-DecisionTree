# NetLogo-DecisionTree Extension

An extension to learn decision trees inside NetLogo. Based on Weka's implementation.

## Building

Use Ant to build.

```
git clone https://github.com/CarlosManuelRodr/NetLogo-DecisionTree
cd NetLogo-DecisionTree
ant
```

If compilation succeeds, `decision-tree.jar` will be created in the `decision-tree` directory.

## Using

Add the directory `decision-tree` to your NetLogo project directory. This must contain the extension file `decision-tree.jar` and the weka library `weka.jar`. You can import the extension with:

```
extensions [decision-tree]
```

### Creating a classifier

To create a classifier you need to provide first the attributes that will be used in the instances. Also, you need the specify the type of argument, numeric with an empty list or nominal with a list of possible values. Numeric attributes are automatically discretized by the classifier. Finally, you need to specify the index of the class that will be predicted.
Here is an example:

```NetLogo
decision-tree:make-classifier
["sepal-length" "sepal-width" "petal-length" "petal-width" "species"]
[[] [] [] [] ["setosa" "versicolor" "virginica"]] 4
```

### Adding training instances to the classifier

To add an instance to the classifier you will need to put every individual attribute-value pair in the instance, and then it can be passed to the classifier. The classifier is not trained yet, this is so in order to be able to put multiple instances without the need to retrain every time an instance is added.

```NetLogo
  let instance decision-tree:make-instance
  decision-tree:put-instance instance "sepal-length" 5.4
  decision-tree:put-instance instance "sepal-width" 3.4
  decision-tree:put-instance instance "petal-length" 1.5
  decision-tree:put-instance instance "petal-width" 0.4
  decision-tree:put-instance instance "species" "setosa"
  decision-tree:addto-classifier classifier instance
```

### Training and testing the classifier

You can then train the classifier and test a new instance that does not contain the class attribute. 

```NetLogo
decision-tree:train-classifier classifier

let test-instance decision-tree:make-instance
decision-tree:put-instance test-instance "sepal-length" 6.7
decision-tree:put-instance test-instance "sepal-width" 3.1
decision-tree:put-instance test-instance "petal-length" 4.7
decision-tree:put-instance test-instance "petal-width" 1.5

show decision-tree:classify classifier test-instance
```

## Primitives

  [`decision-tree:make-classifier`](#decision-tree:make-classifier)
  [`decision-tree:clear-classifier`](#decision-tree:clear-classifier)
  [`decision-tree:addto-classifier`](#decision-tree:addto-classifier)
  [`decision-tree:train-classifier`](#decision-tree:train-classifier)
  [`decision-tree:classify`](#decision-tree:classify)
  [`decision-tree:make-instance`](#decision-tree:make-instance)
  [`decision-tree:put-instance`](#decision-tree:put-instance)

### `decision-tree:make-classifier`

```NetLogo
decision-tree:make-classifier [attribute_names] [attribute_types] <class_index>
```

Creates a classifier object based on Weka's J48 Classifier. When shown on the inspector it prints the classification tree.

The argument `[attribute_names]` is a list containing the names of the variables that will be given to the classifier. The list `[attribute_types]` specifies wether the attribute in the same index of `[attribute_names]` is numeric or nominal. Possible values are an empty list `[]` if the attribute is numeric and a list containing the possible values if the attribute is nominal. `<class_index>` is the index of the class to be predicted.

#### Example:

```
decision-tree:make-classifier
["sepal-length" "sepal-width" "petal-length" "petal-width" "species"]
[[] [] [] [] ["setosa" "versicolor" "virginica"]] 4`
```

### `decision-tree:clear-classifier`

```NetLogo
decision-tree:clear-classifier <classifier>
```

Clears the instances and learned tree of the classifier.

### `decision-tree:addto-classifier`

```NetLogo
decision-tree:addto-classifier <classifier> <instance>
```

Add instance to the classifier. If the instance contains a attribute that is not present in the classifier, it is ignored.

**`decision-tree:train-classifier`**

```NetLogo
decision-tree:train-classifier <classifier>
```

Train the classifier with the given instances.

**`decision-tree:classify`**

```NetLogo
decision-tree:classify <classifier> <instance>
```

Classify instances according to the learned tree.

**`decision-tree:make-instance`**

```NetLogo
decision-tree:make-instance
```

Create an instance object.

**`decision-tree:put-instance`**

```NetLogo
decision-tree:put-instance <instance> <key> <value>
```

Put a key-value pair in the instance.
