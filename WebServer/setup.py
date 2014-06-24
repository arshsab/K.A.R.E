from setuptools import setup, find_packages

requires = [
    'pyramid',
    'pyramid_chameleon',
    'pyramid_debugtoolbar',
    'waitress',
    'cherrypy',
    'pymongo',
]

setup(name='WebServer',
      version='0.0',
      description='Server for KARE.',
      packages=find_packages(),
      install_requires=requires,
      test_suite="webserver",
      entry_points="""\
      [paste.app_factory]
      main = webserver:main
      """,
)
