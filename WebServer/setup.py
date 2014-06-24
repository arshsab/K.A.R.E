from setuptools import setup

setup(
    name='webserver',
    version='',
    packages=[
        'webserver',
        'webserver.data'
    ],
    url='',
    license='',
    author='',
    author_email='',
    description='',
    requires=[
        'cherrypy',
        'pyramid',
        'pymongo',
        'pyramid_mako'
    ],
    entry_points={
        'setuptools.installation': [
            'eggsecutable = webserver.main:run_server',
        ]
    }
)
