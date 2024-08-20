import socket

# Crear un socket TCP
HOST = '0.0.0.0'  # Escucha en todas las interfaces
PORT = 5001
with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
    s.bind((HOST, PORT))
    s.listen()
    print(f'Escuchando en el puerto {PORT}')

    while True:
        conn, addr = s.accept()
        with conn:
            print('Conexión establecida desde', addr)
            while True:
                data = conn.recv(1024)
                if not data:
                    break
                print(data.decode())