
directory="/Users/ralphacastilleja/Downloads/JDSAppData"

for file in "$directory"/*; do
    chmod 644 "$file"
done
