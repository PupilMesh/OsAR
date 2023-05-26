import requests
import os
import img2pdf
from PIL import Image
from io import BytesIO

# make sure you're in the directory you want to be in
os.chdir('C:\\Users\\Krishnan\\Desktop\\PupilMesh\\PMeshRepo\\OsAR\\packages\\mobile\\android\\app\\src\\main\\python\\')

base_url = "https://image.slidesharecdn.com/handbook-public-v5-180105043849/75/volunteers-handbook-publicv51-{}-2048.jpg?cb=1665604938"

image_files = []

for i in range(1, 48):
    url = base_url.format(i)
    response = requests.get(url)

    if response.status_code == 200:  # if request was successful
        img = Image.open(BytesIO(response.content))  # open image from the response content
        img_filename = f'image_{i}.jpg'
        img.save(img_filename)  # save image
        image_files.append(img_filename)  # add image file name to the list
    else:
        print(f"Failed to download image at {url}")

# Now, convert all the images into a single PDF file
with open("output.pdf","wb") as f:
    f.write(img2pdf.convert(image_files))

# Optionally, delete the image files after converting to PDF
for img_filename in image_files:
    os.remove(img_filename)

print("Done!")
