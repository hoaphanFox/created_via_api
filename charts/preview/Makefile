OS := $(shell uname)

preview: 
ifeq ($(OS),Darwin)
	sed -i "" -e "s/version:.*/version: $(PREVIEW_VERSION)/" Chart.yaml
	sed -i "" -e "s/version:.*/version: $(PREVIEW_VERSION)/" ../*/Chart.yaml
	sed -i "" -e "s/tag:.*/tag: $(PREVIEW_VERSION)/" values.yaml
else ifeq ($(OS),Linux)
	sed -i -e "s/version:.*/version: $(PREVIEW_VERSION)/" Chart.yaml
	sed -i -e "s/version:.*/version: $(PREVIEW_VERSION)/" ../*/Chart.yaml
#	sed -i -e "s|repository:.*|repository: $(DOCKER_REGISTRY)\/customize!project.org\/customize!project.name|" values.yaml
	sed -i -e "s|repository:.*|repository: $(DOCKER_REGISTRY)\/fsa-streamotion\/bedrock-streamotion-webflux-k8s|" values.yaml # Please do not edit this line! Managed by customize.sh
	sed -i -e "s/tag:.*/tag: $(PREVIEW_VERSION)/" values.yaml
else
	echo "platfrom $(OS) not supported to release from"
	exit -1
endif
	echo "  version: $(PREVIEW_VERSION)" >> requirements.yaml
	jx step helm build
