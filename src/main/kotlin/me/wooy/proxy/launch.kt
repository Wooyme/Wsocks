package me.wooy.proxy

import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.wooy.proxy.client.ClientSockJs
import me.wooy.proxy.server.ServerSockJs
import me.wooy.proxy.ui.ClientUI
import me.wooy.proxy.ui.TestTray
import org.apache.commons.cli.*


fun main(args:Array<String>) {
  val vertx = Vertx.vertx()
  val options = options()
  val parser = DefaultParser()
  val formatter = HelpFormatter()
  val cmd = try{
    parser.parse(options,args)
  }catch (e:ParseException){
    formatter.printHelp("utility-name", options)
    return
  }
  GlobalScope.launch(vertx.dispatcher()) {
    when(cmd.getOptionValue("type")){
      "client-ui"->{
        val clientConfig = JsonObject().put("ui",true)
        awaitResult<String> {
          vertx.deployVerticle(ClientSockJs(), DeploymentOptions().setConfig(clientConfig), it)
        }
        awaitResult<String> {
          vertx.deployVerticle(ClientUI(),it)
        }
      }
      "client"->{
        val user = cmd.getOptionValue("user")
        val pass = cmd.getOptionValue("pass")
        val localPort = cmd.getOptionValue("local-port").toInt()
        val remotePort = cmd.getOptionValue("remote-port").toInt()
        val remoteIp = cmd.getOptionValue("remote-ip")
        val clientConfig = JsonObject()
          .put("local.port",localPort)
          .put("remote.ip", remoteIp)
          .put("remote.port",remotePort)
          .put("user",user)
          .put("pass",pass)
        awaitResult<String> {
          vertx.deployVerticle(ClientSockJs(), DeploymentOptions().setConfig(clientConfig), it)
        }
      }
      "server"->{
        val usersFile = cmd.getOptionValue("config-user")
        val localPort = cmd.getOptionValue("local-port").toInt()
        val serverConfig = JsonObject().put("port",localPort).put("users",usersFile)
        awaitResult<String> {
          vertx.deployVerticle(ServerSockJs(),DeploymentOptions().setConfig(serverConfig), it)
        }
      }
      "both"->{
        val port = 9888
        val userListFile = "config.json"
        val serverConfig = JsonObject()
          .put("port",port)
          .put("users",userListFile)
        awaitResult<String> {
          vertx.deployVerticle(ServerSockJs(),DeploymentOptions().setConfig(serverConfig), it)
        }
        val clientConfig = JsonObject().put("ui",true)
        awaitResult<String> {
          vertx.deployVerticle(ClientSockJs(), DeploymentOptions().setConfig(clientConfig), it)
        }
        awaitResult<String> {
          vertx.deployVerticle(ClientUI(),it)
        }
      }
    }
  }
}


fun options():Options{
  val options = Options()
  val proxyType = Option("T","type",true,"[server/client/client-ui]")
  proxyType.isRequired = true
  options.addOption(proxyType)

  val localPort = Option("LP","local-port",true,"Local port for client/server")
  localPort.isRequired = false
  options.addOption(localPort)

  val remoteIp = Option("RI","remote-ip",true,"Remote ip for client")
  remoteIp.isRequired = false
  options.addOption(remoteIp)

  val remotePort = Option("RP","remote-port",true,"Remote port for client")
  remotePort.isRequired = false
  options.addOption(remotePort)

  val user = Option("U","user",true,"Username")
  user.isRequired = false
  options.addOption(user)

  val pwd = Option("P","pass",true,"Password")
  pwd.isRequired = false
  options.addOption(pwd)

  val usersFile = Option("C","config-user",true,"User list file for server")
  pwd.isRequired = false
  options.addOption(usersFile)
  return options
}