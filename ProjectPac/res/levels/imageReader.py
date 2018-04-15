filename = "rock2"
file = open(filename + ".txt", "w+")

from matplotlib.image import imread
image = imread(filename + ".png")


for i in range(image.shape[0]):
	for j in range(image.shape[1]):
		if (image[i][j][0] == 0 and image[i][j][1] == 0 and image[i][j][2] == 0):	## Black => Wall
			file.write("1")
		elif (image[i][j][0] == 1 and image[i][j][1] == 0 and image[i][j][2] == 0): ## Red => Enemy
			file.write("-8")
		elif (image[i][j][1] == 1 and image[i][j][0] == 0 and image[i][j][2] == 0): ## Green => Pellets
			file.write("4")
		elif (image[i][j][2] == 1 and image[i][j][0] == 0 and image[i][j][1] == 0): ## Blue => Power Pellets
			file.write("5")
		elif (image[i][j][0] == 1 and image[i][j][1] == 1 and image[i][j][2] == 0): ## Yellow => Player
			file.write("2")
		else:						  												## White => Nothing
			file.write("0")
		file.write(",")
	
	file.write("\n")

file.close()