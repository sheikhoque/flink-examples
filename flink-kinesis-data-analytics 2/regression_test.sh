#!/bin/bash

MAX_WAIT_TIME=900
SUITE_NAME="Regression: "$2


case $1 in

   WTViphone)
       testCase="240,164,145,144,183,153,175,179,180,181,218,235,245,248,249,258,264,244,287,108,284,282,283,271,272,286,289,290,291,293,295,301,304,313,314,319,320,317"
       host='53'
       deviceType='22'
       testConfig='51,41,51,51,51,51,51,51,51,51,51,51,51,51,51,51,51,51,51,51,51,51,51,51,51,51,51,51,51,51,51,51,51,51,51,51,51,51'
       testIterations='1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1'
       echo WatchTV iPhone Regression Test Started
       ;;

   WTViPad)
       testCase="240,186,145,144,183,238,209,213,210,235,245,258,264,244,285,108,284,282,283,286,292,293,295,302,304,312,315,271,319,321,317,341"
       host='41'
       deviceType='32'
       testConfig='52,43,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52'
       testIterations='1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1'
       echo WatchTV iPad Regression Test Started
       ;;

   WTVandroidTablet)
       testCase="243,145,144,183,238,209,213,235,245,258,264,244,285,108,284,282,283,286,292,293,295,302,304,312,315,271,319,321,317,341"
       host='48'
       deviceType='30'
       testConfig='54'
       testIterations='1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1'
       echo WatchTV android Tablet Regression Test Started
       ;;

   WTVandroidMobile)
       testCase="243,145,144,183,153,175,179,180,218,235,245,248,249,258,264,244,287,108,284,282,283,271,272,286,289,290,291,293,295,301,304,313,314,319,320,317"
       host='51'
       deviceType='38'
       testConfig='53'
       testIterations='1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1'
       echo WatchTV android Mobile Regression Test Started
       ;;

   WTVfireTV)
       testCase="251,166,168,170,171,172,173,245,246,247,253,261,265,298,342"
       host='52'
       deviceType='37'
       testConfig='55'
       testIterations='1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1'
       echo WatchTV FireTV Regression Test Started
       ;;

   *androidTablet*)
       testCase="326,145,235,144,183,224,209,210,212,213,217,219,228,238,110,258,264,244,285,108,284,282,283,286,292,293,295,300,304,306,309,315,319,321,327,323,341,336"
       host='48'
       deviceType='30'
       testConfig='70'
       testIterations='1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1'
       echo Android Tablet Regression Test Started
       #MSDC09 Android Tab
       # testCase_new09="143,145,235,144,183,224,209,210,212,213,217,219,228,238,110,258,264,244,285,108,284,282,283,286,292,293,295,300,304,306,309,315,319,321"
       # host_new09='64'
       # deviceType_new09='47'
       # testConfig_new09='68'
       # testIterations_new09='1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1'
       ;;

   *androidMobile*)
       testCase="326,145,148,235,144,183,153,158,175,179,180,181,216,218,228,110,258,264,244,287,108,284,282,283,272,286,289,290,291,293,295,299,304,308,314,319,320,327,323,336"
       host='51'
       deviceType='38'
       testConfig='72'
       testIterations='1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1'
       #MDSC06 Infra Test for Resiliency test
       test_IDs_06=$(curl -s -XPOST -F "test-config=56" -F "host=55" -F "device-type=39" -F "test-type=Devices" -F "test-case=347" -F "test-iterations=1" -F "test-suite-name=$SUITE_NAME" -F "build_version=$2" -H "Authorization: Token f595d10ef19245a7e7d23dea53aad8236d4a44c5" -H "Referer: http://smart-uri.cld.dtveng.net/test/test-case" http://smart-api.cld.dtveng.net/api/execute/ |jq .test_result_id)
       echo Android Mobile Regression Test Started
       #MSDC06 AndroidMobile
       # testCase_new06="143,145,148,235,144,183,153,158,175,179,180,181,216,218,228,110,258,264,244,287,108,284,282,283,272,286,289,290,291,293,295,299,304,308,314,319,320"
       # host_new06='55'
       # deviceType_new06='39'
       # testConfig_new06='56'
       # testIterations_new06='1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1'
       ;;

   *fireTV*)
       testCase="226,166,168,170,171,172,173,174,229,253,260,265,269,270,298,322,342"
       host='52'
       deviceType='37'
       testConfig='73'
       testIterations='1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1'
       echo FireTV Regression Test Started
       #MSDC07 FireTV
       # testCase_new07="138,166,168,170,171,172,173,174,229,253,260,265,269,270,298"
       # host_new07='62'
       # deviceType_new07='43'
       # testConfig_new07='61'
       # testIterations_new07='1,1,1,1,1,1,1,1,1,1,1,1,1,1,1'
       ;;

   *iPhone*)
       testCase="324,164,145,148,235,144,183,158,175,179,180,181,216,218,228,110,258,264,244,287,108,284,282,283,272,286,289,290,291,293,295,299,304,308,314,319,320,327,323,336"
       host='53'
       deviceType='22'
       testConfig='71,41,71,71,71,71,71,71,71,71,71,71,71,71,71,71,71,71,71,71,71,71,71,71,71,71,71,71,71,71,71,71,71,71,71,71,71,71,71,71'
       testIterations='1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1'
       echo iPhone Regression Test Started
       #MSDC07 iPhone
       #testCase_new07="277,163,164,145,148,235,144,183,158,175,179,180,181,216,218,228,110,258,264,244,287,278,108,284,282,283,272,286,289,290,291,293,295,299,304,308,314,319,320"
       #host_new07='61'
       #deviceType_new07='44'
       #testConfig_new07='62,62,63,62,62,62,62,62,62,62,62,62,62,62,62,62,62,62,62,62,62,62,62,62,62,62,62,62,62,62,62,62,62,62,62,62,62,62,62'
       #testIterations_new07='1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1'
       #MSDC09 iPhone
       #testCase_new09="163,164,145,148,235,144,183,158,175,179,180,181,216,218,228,110,258,264,244,287,108,284,282,283,272,286,289,290,291,293,295,299,304,308,314,319,320"
       #host_new09='58'
       #deviceType_new09='46'
       #testConfig_new09='66,67,66,66,66,66,66,66,66,66,66,66,66,66,66,66,66,66,66,66,66,66,66,66,66,66,66,66,66,66,66,66,66,66,66,66,66'
       #testIterations_new09='1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1'
       ;;

   *iPad*)
       testCase="324,186,145,235,144,183,238,224,209,210,212,213,217,219,228,110,258,264,244,285,108,284,282,283,286,292,293,295,300,304,306,309,315,319,321,327,323,341,336"
       host='41'
       deviceType='32'
       testConfig='69,43,69,69,69,69,69,69,69,69,69,69,69,69,69,69,69,69,69,69,69,69,69,69,69,69,69,69,69,69,69,69,69,69,69,69,69,69,69'
       testIterations='1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1'
       echo iPad Regression Test Started
       #testCase_new06="163,186,145,235,144,183,238,224,209,210,212,213,217,219,228,110,258,264,244,285,108,284,282,283,286,292,293,295,300,304,306,309,315,319,321"
       #host_new06='57'
       #deviceType_new06='41'
       #testConfig_new06='58,59,58,58,58,58,58,58,58,58,58,58,58,58,58,58,58,58,58,58,58,58,58,58,58,58,58,58,58,58,58,58,58,58,58'
       #testIterations_new06='1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1'
       ;;

   *)
      echo Device Type not recognized.  Please use a valid device type: androidMobile, androidTablet,  fireTV, iPhone, iPad
      exit 1
      ;;
esac

# Start the test execution.  All tests will be Queued
test_IDs=$(curl -s -XPOST -F "test-config=$testConfig" -F "host=$host" -F "device-type=$deviceType" -F "test-type=Devices" -F "test-case="+$testCase -F "test-iterations=$testIterations" -F "test-suite-name=$SUITE_NAME" -F "build_version=$2" -H "Authorization: Token f595d10ef19245a7e7d23dea53aad8236d4a44c5" -H "Referer: http://smart-uri.cld.dtveng.net/test/test-case" http://smart-api.cld.dtveng.net/api/execute/ |jq .test_result_id)
echo "SMART Test initiated, TestIDs: $test_IDs"

#echo "SMART Test initiated, TestIDs_06: $test_IDs_06"
#MDSC07 Infra Test
#test_IDs_07=$(curl -s -XPOST -F "test-config=$testConfig_new07" -F "host=$host_new07" -F "device-type=$deviceType_new07" -F "test-type=Devices" -F "test-case="+$testCase_new07 -F "test-iterations=$testIterations_new07" -F "test-suite-name=$SUITE_NAME" -F "build_version=$2" -H "Authorization: Token f595d10ef19245a7e7d23dea53aad8236d4a44c5" -H "Referer: http://smart-uri.cld.dtveng.net/test/test-case" http://smart-api.cld.dtveng.net/api/execute/ |jq .test_result_id)
#echo "SMART Test initiated, TestIDs_07: $test_IDs_07"
#MDSC09 Infra Test
#test_IDs_09=$(curl -s -XPOST -F "test-config=$testConfig_new09" -F "host=$host_new09" -F "device-type=$deviceType_new09" -F "test-type=Devices" -F "test-case="+$testCase_new09 -F "test-iterations=$testIterations_new09" -F "test-suite-name=$SUITE_NAME" -F "build_version=$2" -H "Authorization: Token f595d10ef19245a7e7d23dea53aad8236d4a44c5" -H "Referer: http://smart-uri.cld.dtveng.net/test/test-case" http://smart-api.cld.dtveng.net/api/execute/ |jq .test_result_id)
#echo "SMART Test initiated, TestIDs_09: $test_IDs_09"
#split the list of Test IDs into an array
temp_ifs=IFS
IFS=',' read -a arr <<< "$test_IDs"
arr=($test_IDs)
IFS=temp_ifs

# shellcheck disable=SC2068
for testCase in ${arr[@]};
do
    # Get rid of the unused comma
    testId=${testCase//,}
    echo "testID $testId"

    # Check if this is a valid ID
    re='^[0-9]+$'
    if ! [[ $testId =~ $re ]] ; then
      continue
    fi

    # Proceed to check the status of the test
    start_time=`date +%s`
    while true
    do
        sleep 5s
        current_time=`date +%s`
        delta_time=$((current_time-start_time))
        testStatus=$(curl -s -H "Authorization: Token f595d10ef19245a7e7d23dea53aad8236d4a44c5" -H "Referer: http://smart-uri.cld.dtveng.net/test/test-results/" "http://smart-api.cld.dtveng.net/api/test-results/$testId/" | jq .test_case_status | tr -d '\n')
        echo "Test Status: $testStatus"
        if [[ $testStatus == *PASS* ]]
        then
            break
        elif [[ $testStatus == *FAIL* ]]
        then
            echo "Test failed, TestID= test_$testId "
            exit $testId
        elif [[ $delta_time -gt $MAX_WAIT_TIME ]]
        then
            echo Error: Tests timed out.
            exit 1
        fi
    done
done
echo $1 Regression Test Complete
