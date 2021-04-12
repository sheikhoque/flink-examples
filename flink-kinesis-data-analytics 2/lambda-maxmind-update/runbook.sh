# CF commands
aws cloudformation validate-template --template-body file://template.yaml --profile att
aws cloudformation create-stack --stack-name MaxmindDBUpdate --template-body file://template.yaml --capabilities CAPABILITY_AUTO_EXPAND --profile att
aws cloudformation delete-stack --stack-name MaxmindDBUpdate --profile att && aws cloudformation wait stack-delete-complete --stack-name MaxmindDBUpdate --profile att && aws cloudformation create-stack --stack-name MaxmindDBUpdate --template-body file://template.yaml --capabilities CAPABILITY_AUTO_EXPAND --profile att