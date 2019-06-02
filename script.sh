numRequests=$1
counter=0
for ((request=1;request<=$numRequests;request++))
do
    for ((x=1;x<=2;x++))
    do
    counter=$((counter+1))
	value=$(seq 0 .01 777 | shuf | head -n1)
#	echo $value
        curl -XPOST -d '{"amount": '$value' , "timestamp":'$(date +"%s")' , "package": "1"}' "http://localhost:8080/transactions" &
    done
done

echo "Total transaction requests ..$counter	"

#curl -X GET "http://localhost:8080/statistics" 
count=$(curl -sb -H "Accept: application/json" "http://localhost:8080/transactionCompletedCount")
echo $count
while [[ $count != 0 ]]; do
	cn=$(curl -sb -H "Accept: application/json" "http://localhost:8080/transactionCompletedCount")
	count=$((cn))
done
echo "Transactions completed counter from server $count.."

response=$(curl -sb -H "Accept: application/json" "http://localhost:8080/statistics")
echo $response
#curl -X GET "http://localhost:8080/poolInfo"
response=$(curl -sb -H "Accept: application/json" "http://localhost:8080/poolInfo")
echo $response

wait

