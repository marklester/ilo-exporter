package main

import (
	"fmt"
	"net/http"
	"time"
	"crypto/tls"
	"io/ioutil"
	"log"
)
const (
	userid=""
	password=""
)
func main(){
  tr := &http.Transport{
	MaxIdleConns:       10,
	IdleConnTimeout:    30 * time.Second,
	DisableCompression: true,
	TLSClientConfig: &tls.Config{InsecureSkipVerify: true},
  }
  client := &http.Client{Transport: tr}
  req,err := http.NewRequest("GET","https://192.168.2.2/redfish/v1/systems/1/" ,nil)
  if(err !=nil){
	fmt.Println(err)
  }

  req.SetBasicAuth(userid,password)
  
  resp,err := client.Do(req)
  if(err !=nil){
	fmt.Println(err)
  }
  b, err := ioutil.ReadAll(resp.Body)
  resp.Body.Close()
  if err != nil {
      log.Fatal(err)
  }
  fmt.Println(string(b))
}
