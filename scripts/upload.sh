if [[ "$TRAVIS_BRANCH" != "master" ]]; then
  echo "Testing on a branch other than master. No deployment will be done."
  exit 0
fi

RELEASE_DATE=`date '+%Y-%m-%d %H:%M:%S'`
RELEASE_NOTES="Branch: $TRAVIS_BRANCH\nBuild: $TRAVIS_BUILD_NUMBER\nUploaded: $RELEASE_DATE"


  echo ""
  echo "***************************"
  echo "* Uploading to HockeyApp *"
  echo "***************************"
  curl \
    -F "status=2" \
    -F "notify=1" \
    -F "notes=$RELEASE_NOTES" \
    -F "notes_type=0" \
    -F "ipa=@$TRAVIS_BUILD_DIR/Zoomlee/app/build/outputs/apk/app-debug.apk" \
    -H "X-HockeyAppToken: c88dcc7839dc4ba7bbaafc9c2ebf40a9" \
    https://rink.hockeyapp.net/api/2/apps/upload
