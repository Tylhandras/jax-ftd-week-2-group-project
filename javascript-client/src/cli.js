import net from 'net'
import vorpal from 'vorpal'

const cli = vorpal()

// cli config
cli
  .delimiter('ftd-chat~$')

// connect mode
let server

cli
  .mode('connect <username> <port> [host]')
  // .delimiter('connected:')
  .init(function (args, callback) {
    server = net.createConnection(args, () => {
      server.write(args.username + '\n')
      this.delimiter(`${args.username}:`)
      callback()
    })

    server.on('data', (data) => {
      this.log(data.toString())
    })

    server.on('end', () => {
      this.log('Disconnected from server')
    })
  })
  .action(function (command, callback) {
    server.write(command + '\n')
    callback()
  })

export default cli
