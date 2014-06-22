import time
import subprocess


def spawn(cmd, output_file):
    return subprocess.Popen(cmd, stdout=output_file, stderr=subprocess.STDOUT)

def rerun_process():
    curr_time = int(time.time())

    proc = spawn('/usr/bin/java -Xms256m -Xmx512m -jar UpdateCycle.jar kare.properties'.split(),
                 open('logs/{0}-update'.format(curr_time), 'wb'))

    print('Rerunning the update cycle.')

    proc.wait()

    print('The update cycle has finished.')

if __name__ == '__main__':
    while True:
        start = time.time()
        proc = rerun_process()
        end = time.time()

        total = ((end - start) / 3600)
        print('The process took: {0} total hours'.format(total))

        time.sleep(1)